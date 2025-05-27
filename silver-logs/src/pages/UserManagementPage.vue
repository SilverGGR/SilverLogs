<template>
  <q-page padding>
    <div class="q-pa-md">
      <div class="row items-center q-mb-md">
        <div class="text-h5 q-mr-auto">Benutzerverwaltung</div>
        <q-btn color="primary" icon="add" label="Neuer Benutzer" @click="openUserDialog()" />
      </div>

      <div class="q-mb-md">
        <q-input
          v-model="filter"
          dense
          outlined
          placeholder="Suche nach Benutzernamen, E-Mail, Name..."
          class="full-width"
        >
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </div>

      <q-table
        :rows="filteredUsers"
        :columns="columns"
        row-key="id"
        :loading="loading"
        :filter="filter"
        :pagination="pagination"
      >
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td key="username" :props="props">{{ props.row.username }}</q-td>
            <q-td key="email" :props="props">{{ props.row.email }}</q-td>
            <q-td key="fullName" :props="props">
              {{ props.row.firstname }} {{ props.row.lastname }}
            </q-td>
            <q-td key="role" :props="props">
              <q-chip :color="getRoleColor(props.row.role)" text-color="white">
                {{ props.row.role }}
              </q-chip>
            </q-td>
            <q-td key="actions" :props="props" class="q-gutter-xs">
              <q-btn
                flat
                round
                dense
                color="primary"
                icon="edit"
                @click="openUserDialog(props.row)"
              />
              <q-btn
                flat
                round
                dense
                color="negative"
                icon="delete"
                @click="confirmDelete(props.row)"
              />
            </q-td>
          </q-tr>
        </template>

        <template v-slot:no-data>
          <div class="full-width text-center q-pa-md">
            Keine Benutzer gefunden
          </div>
        </template>
      </q-table>
    </div>

    <!-- Dialog für Benutzer anlegen/bearbeiten -->
    <q-dialog v-model="userDialogOpen" persistent>
      <q-card style="min-width: 450px">
        <q-card-section>
          <div class="text-h6">{{ isEditMode ? 'Benutzer bearbeiten' : 'Neuer Benutzer' }}</div>
        </q-card-section>

        <q-card-section class="q-pt-none">
          <q-form @submit="saveUser" class="q-gutter-md">
            <q-input
              v-model="userForm.username"
              label="Benutzername"
              :disable="isEditMode"
              :rules="[val => !!val || 'Benutzername wird benötigt']"
            />

            <div class="row q-col-gutter-md">
              <div class="col-12 col-sm-6">
                <q-input
                  v-model="userForm.firstname"
                  label="Vorname"
                  :rules="[val => !!val || 'Vorname wird benötigt']"
                />
              </div>
              <div class="col-12 col-sm-6">
                <q-input
                  v-model="userForm.lastname"
                  label="Nachname"
                  :rules="[val => !!val || 'Nachname wird benötigt']"
                />
              </div>
            </div>

            <q-input
              v-model="userForm.email"
              label="E-Mail"
              type="email"
              :rules="[
                val => !!val || 'E-Mail wird benötigt',
                val => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val) || 'Ungültige E-Mail-Adresse'
              ]"
            />

            <q-input
              v-model="userForm.phone"
              label="Telefon"
            />

            <q-input
              v-model="userForm.department"
              label="Abteilung"
            />

            <q-select
              v-model="userForm.role"
              :options="roleOptions"
              label="Rolle"
              emit-value
              map-options
              :rules="[val => !!val || 'Rolle wird benötigt']"
            />

            <q-input
              v-if="!isEditMode"
              v-model="userForm.password"
              label="Passwort"
              type="password"
              :rules="[val => !!val || 'Passwort wird benötigt']"
            />

            <div class="row justify-end q-mt-md">
              <q-btn label="Abbrechen" color="grey" flat v-close-popup />
              <q-btn label="Speichern" type="submit" color="primary" class="q-ml-sm" />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>

    <!-- Dialog zum Löschen bestätigen -->
    <q-dialog v-model="deleteConfirmOpen" persistent>
      <q-card>
        <q-card-section class="row items-center">
          <q-avatar icon="delete" color="negative" text-color="white" />
          <span class="q-ml-sm">Möchten Sie den Benutzer <b>{{ userToDelete?.username }}</b> wirklich löschen?</span>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Abbrechen" color="grey" v-close-popup />
          <q-btn flat label="Löschen" color="negative" @click="deleteUser" v-close-popup />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <!-- Loading Overlay -->
    <q-inner-loading :showing="isLoading">
      <q-spinner size="50px" color="primary" />
    </q-inner-loading>
  </q-page>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from 'src/boot/axios'
import { useQuasar } from 'quasar'

const $q = useQuasar()
const loading = ref(false)
const isLoading = ref(false)
const users = ref([])
const filter = ref('')
const userDialogOpen = ref(false)
const deleteConfirmOpen = ref(false)
const isEditMode = ref(false)
const userToDelete = ref(null)
const isApprentice = ref(false);

const columns = [
  { name: 'username', label: 'Benutzername', align: 'left', field: 'username', sortable: true },
  { name: 'email', label: 'E-Mail', align: 'left', field: 'email', sortable: true },
  { name: 'fullName', label: 'Name', align: 'left', field: row => `${row.firstname} ${row.lastname}`, sortable: true },
  { name: 'role', label: 'Rolle', align: 'left', field: 'role', sortable: true },
  { name: 'actions', label: 'Aktionen', align: 'center' }
]

const pagination = ref({
  rowsPerPage: 10
})

const roleOptions = [
  { label: 'Benutzer', value: 'USER' },
  { label: 'Supervisor', value: 'SUPERVISOR' },
  { label: 'Administrator', value: 'ADMIN' }
]

const userForm = ref({
  id: null,
  username: '',
  firstname: '',
  lastname: '',
  email: '',
  phone: '',
  department: '',
  role: 'USER',
  password: ''
})

const filteredUsers = computed(() => {
  if (!filter.value) {
    return users.value
  }

  const searchTerm = filter.value.toLowerCase()
  return users.value.filter(user => {
    return user.username.toLowerCase().includes(searchTerm) ||
      user.email.toLowerCase().includes(searchTerm) ||
      user.firstname.toLowerCase().includes(searchTerm) ||
      user.lastname.toLowerCase().includes(searchTerm)
  })
})

function getRoleColor(role) {
  switch (role) {
    case 'ADMIN':
      return 'deep-purple'
    case 'SUPERVISOR':
      return 'teal'
    default:
      return 'blue'
  }
}

async function loadUsers() {
  try {
    loading.value = true
    const response = await api.get('/api/authUser/all')
    users.value = response.data
    console.log(users.value)
  } catch (error) {
    console.error('Fehler beim Laden der Benutzer:', error)
    $q.notify({
      type: 'negative',
      message: 'Benutzer konnten nicht geladen werden'
    })
  } finally {
    loading.value = false
  }
}

function openUserDialog(user = null) {
  if (user) {
    // Bearbeiten eines bestehenden Benutzers
    isEditMode.value = true
    userForm.value = {
      id: user.id,
      username: user.username,
      firstname: user.firstname,
      lastname: user.lastname,
      email: user.email,
      phone: user.phone || '',
      department: user.department || '',
      role: user.role
    }
  } else {
    // Neuen Benutzer erstellen
    isEditMode.value = false
    userForm.value = {
      id: null,
      username: '',
      firstname: '',
      lastname: '',
      email: '',
      phone: '',
      department: '',
      role: 'USER',
      password: ''
    }
  }
  userDialogOpen.value = true
}

async function saveUser() {
  try {
    isLoading.value = true

    if (isEditMode.value) {
      // Benutzer aktualisieren
      await api.put(`/api/authUser/admin/update/${userForm.value.id}`, userForm.value)
      $q.notify({
        type: 'positive',
        message: 'Benutzer erfolgreich aktualisiert'
      })
    } else {
      // Neuen Benutzer erstellen
      if (isApprentice.value) {
        await api.post('/api/authUser/admin/createApprentice', userForm.value)
        $q.notify({
          type: 'positive',
          message: 'Benutzer erfolgreich erstellt'
        })
      } else {
        await api.post('/api/authUser/admin/create', userForm.value)
        $q.notify({
          type: 'positive',
          message: 'Benutzer erfolgreich erstellt'
        })
      }
    }

    userDialogOpen.value = false
    await loadUsers()

  } catch (error) {
    console.error('Fehler beim Speichern des Benutzers:', error)
    $q.notify({
      type: 'negative',
      message: error.response?.data?.message || 'Fehler beim Speichern des Benutzers'
    })
  } finally {
    isLoading.value = false
  }
}

function confirmDelete(user) {
  userToDelete.value = user
  deleteConfirmOpen.value = true
}

async function deleteUser() {
  if (!userToDelete.value) return

  try {
    isLoading.value = true
    await api.delete(`/api/authUser/admin/delete/${userToDelete.value.username}`)
    $q.notify({
      type: 'positive',
      message: 'Benutzer erfolgreich gelöscht'
    })
    await loadUsers()
  } catch (error) {
    console.error('Fehler beim Löschen des Benutzers:', error)
    $q.notify({
      type: 'negative',
      message: error.response?.data?.message || 'Fehler beim Löschen des Benutzers'
    })
  } finally {
    isLoading.value = false
    userToDelete.value = null
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
.q-table__card {
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
}
</style>
