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
        :row-key="row => row.username"
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
    <q-dialog v-model="userDialogOpen" persistent>
      <q-card>
        <q-card-section>
          <div class="text-h6">{{ isEditMode ? 'Benutzer bearbeiten' : 'Neuer Benutzer' }}</div>
          <q-toggle v-model="isApprentice" label="Apprentice" />
        </q-card-section>

        <q-card-section class="q-pt-none q-pd-md">
          <q-form @submit="saveUser" class="q-gutter-md">
            <div class="row q-col-gutter-md q-px-lg">
              <div class="col-12 col-md-6">
                <q-input
                  v-model="userForm.username"
                  label="Benutzername"
                  :rules="[val => !!val || 'Benutzername wird benötigt']"
                />
              </div>

              <div class="col-12 col-md-6">
                <q-input
                  v-model="userForm.email"
                  label="E-Mail"
                  type="email"
                  :rules="[
                val => !!val || 'E-Mail wird benötigt',
                val => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val) || 'Ungültige E-Mail-Adresse'
              ]"
                />
              </div>

              <div class="col-12 col-md-6">
                <q-input
                  v-model="userForm.firstname"
                  label="Vorname"
                  :rules="[val => !!val || 'Vorname wird benötigt']"
                />
              </div>

              <div class="col-12 col-md-6">
                <q-input
                  v-model="userForm.lastname"
                  label="Nachname"
                  :rules="[val => !!val || 'Nachname wird benötigt']"
                />
              </div>

              <div class="col-12 col-md-6">
                <q-input
                  v-model="userForm.phone"
                  label="Telefon"
                />
              </div>

              <div class="col-12 col-md-6">
                <q-input
                  v-model="userForm.department"
                  label="Abteilung"
                />
              </div>

              <div class="col-12 col-md-6">
                <q-select
                  v-model="userForm.role"
                  :options="roleOptions"
                  label="Rolle"
                  emit-value
                  map-options
                  :rules="[val => !!val || 'Rolle wird benötigt']"
                  :disable="isApprentice"
                />
              </div>

              <div class="col-12 col-md-6" v-if="!isEditMode">
                <q-input
                  v-model="userForm.password"
                  label="Passwort"
                  type="password"
                  :rules="[val => !!val || 'Passwort wird benötigt']"
                />
              </div>

              <div class="col-12 col-md-6" v-if="isApprentice">
                <q-input
                  v-model="userForm.startingDate"
                  label="Startdatum"
                  type="date"
                  :rules="[val => !!val || 'Startdatum wird benötigt']"
                />
              </div>

              <div class="col-12 col-md-6" v-if="isApprentice">
                <q-input
                  v-model="userForm.endingDate"
                  label="Enddatum"
                  type="date"
                  :rules="[val => !!val || 'Enddatum wird benötigt']"
                />
              </div>
            </div>

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
//TODO: role value geht noch ned (Innerhalb des Dialogs beim umschalten von Apprentice)
import { ref, computed, onMounted, watch } from 'vue'
import { api } from 'src/boot/axios'
import { useQuasar } from 'quasar'
import AuthUserDto from 'src/dtos/AuthUserDto.js'
import ApprenticeDto from 'src/dtos/ApprenticeDto.js'

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

const userForm = ref(createEmptyUser())

function createEmptyUser() {
  if (isApprentice.value) {
    // LocalDate passend setzen/anpassen
    return new ApprenticeDto(
      null,                 // startingDate
      null,                 // endingDate
      '', '', '', '', '', '', '', '', ''
    )
  }
  return new AuthUserDto('', '', '', '', '', '', 'USER', null, '', '')
}


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
    isEditMode.value = true
    if (user.startingDate !== undefined) {
      // Azubi-Formular, inklusive Date-Felder
      isApprentice.value = true
      userForm.value = new ApprenticeDto(
        user.startingDate || null,
        user.endingDate || null,
        user.username || '',
        user.firstname || '',
        user.lastname || '',
        user.email || '',
        user.phone || '',
        user.department || '',
        user.role || 'USER',
        user.profileImage || null,
        user.profileImageType || '',
        // Passwort soll beim Bearbeiten idR leer bleiben
        ''
      )
    } else {
      isApprentice.value = false
      userForm.value = new AuthUserDto(
        user.username || '',
        user.firstname || '',
        user.lastname || '',
        user.email || '',
        user.phone || '',
        user.department || '',
        user.role || 'USER',
        user.profileImage || null,
        user.profileImageType || '',
        ''
      )
    }
  } else {
    isEditMode.value = false
    userForm.value = createEmptyUser()
  }
  userDialogOpen.value = true
}


async function saveUser() {
  try {
    isLoading.value = true
    let apiUrl
    let dtoData = userForm.value

    if (isEditMode.value) {
      apiUrl = '/api/authUser/admin/update'
      // Bei Update api.put verwenden, bei Bedarf Username/ID übergeben
      await api.put(apiUrl, dtoData)
      $q.notify({
        type: 'positive',
        message: 'Benutzer erfolgreich aktualisiert'
      })
      userDialogOpen.value = false
      await loadUsers()
    } else if (isApprentice.value) {
      apiUrl = '/api/apprentice/admin/createApprentice'
      await api.post(apiUrl, dtoData).then(() => {
        $q.notify({
          type: 'positive',
          message: 'Auszubildender erfolgreich erstellt'
        })
        userDialogOpen.value = false
        loadUsers()
      }).catch(error => {
        if (error.response.status === 409) {
          $q.notify({
            type: 'negative',
            message: 'Benutzername bereits vergeben'
          })
        }
      })
    } else {
      apiUrl = '/api/authUser/admin/createUser'
      await api.post(apiUrl, dtoData).then(() => {
        $q.notify({
          type: 'positive',
          message: 'Benutzer erfolgreich erstellt'
        })
        userDialogOpen.value = false
        loadUsers()
      }).catch(error => {
        if (error.response.status === 409) {
          $q.notify({
            type: 'negative',
            message: 'Benutzername bereits vergeben'
          })
        }
      })

    }
  } catch (error) {
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

watch(() => isApprentice, (newValue) => {
  if (newValue) {
    userForm.value.role = 'USER'
  }
})
</script>

<style lang="scss" scoped>
.q-table__card {
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
}
</style>
