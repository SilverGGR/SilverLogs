
<template>
  <q-page padding>
    <div class="row q-col-gutter-md">
      <div class="col-12">
        <h4 class="q-mt-none q-mb-md">Benutzer-Supervisor Verwaltung</h4>
      </div>

      <!-- Apprentice Auswahl -->
      <div class="col-12 col-md-6">
        <q-card class="supervisor-card">
          <q-card-section>
            <div class="text-h6">Auszubildende</div>
          </q-card-section>
          <q-card-section>
            <q-select
              v-model="selectedApprentice"
              :options="apprentices"
              option-label="username"
              label="Auszubildenden auswählen"
              emit-value
              map-options
              clearable
              @update:model-value="loadSupervisorsForApprentice"
            >
              <template v-slot:option="scope">
                <q-item v-bind="scope.itemProps">
                  <q-item-section>
                    <q-item-label>{{ scope.opt.username }}</q-item-label>
                    <q-item-label caption>{{ scope.opt.firstname }} {{ scope.opt.lastname }}</q-item-label>
                  </q-item-section>
                </q-item>
              </template>
            </q-select>
          </q-card-section>
        </q-card>
      </div>

      <!-- Supervisor Auswahl -->
      <div class="col-12 col-md-6">
        <q-card class="supervisor-card">
          <q-card-section>
            <div class="text-h6">Supervisors</div>
          </q-card-section>
          <q-card-section>
            <q-select
              v-model="selectedSupervisor"
              :options="supervisors"
              option-label="username"
              label="Supervisor auswählen"
              emit-value
              map-options
              clearable
              :disable="!selectedApprentice"
            >
              <template v-slot:option="scope">
                <q-item v-bind="scope.itemProps">
                  <q-item-section>
                    <q-item-label>{{ scope.opt.username }}</q-item-label>
                    <q-item-label caption>{{ scope.opt.firstname }} {{ scope.opt.lastname }}</q-item-label>
                  </q-item-section>
                </q-item>
              </template>
            </q-select>
          </q-card-section>
          <q-card-section>
            <q-btn
              color="primary"
              label="Supervisor zuweisen"
              class="q-mr-sm"
              :disable="!canAssignSupervisor"
              @click="assignSupervisor"
            />
          </q-card-section>
        </q-card>
      </div>

      <!-- Zugewiesene Supervisors -->
      <div class="col-12">
        <q-card v-if="selectedApprentice">
          <q-card-section>
            <div class="text-h6">Zugewiesene Supervisors für {{ selectedApprentice?.username }}</div>
          </q-card-section>
          <q-card-section>
            <q-list bordered separator>
              <q-item v-for="supervisor in assignedSupervisors" :key="supervisor.id">
                <q-item-section>
                  <q-item-label>{{ supervisor.username }}</q-item-label>
                  <q-item-label caption>{{ supervisor.firstname }} {{ supervisor.lastname }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-btn
                    flat
                    round
                    color="negative"
                    icon="delete"
                    @click="removeSupervisor(supervisor)"
                  />
                </q-item-section>
              </q-item>
              <q-item v-if="assignedSupervisors.length === 0">
                <q-item-section>
                  <q-item-label>Keine Supervisors zugewiesen</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <!-- Loading Overlay -->
    <q-inner-loading :showing="loading">
      <q-spinner size="50px" color="primary" />
    </q-inner-loading>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { api } from 'src/boot/axios'
import { useAuthStore } from 'src/stores/auth'
import { Notify } from 'quasar'

const authStore = useAuthStore()

// Daten
const apprentices = ref([])
const supervisors = ref([])
const selectedApprentice = ref(null)
const selectedSupervisor = ref(null)
const assignedSupervisors = ref([])
const loading = ref(false)

// Berechnete Eigenschaften
const canAssignSupervisor = computed(() => {
  return selectedApprentice.value &&
    selectedSupervisor.value &&
    !assignedSupervisors.value.some(s => s.id === selectedSupervisor.value.id)
})

// Methoden
const loadApprentices = async () => {
  try {
    loading.value = true
    const response = await api.get('/api/authUser/apprentice/all')
    apprentices.value = response.data
  } catch (error) {
    console.error('Fehler beim Laden der Auszubildenden:', error)
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Laden der Auszubildenden'
    })
  } finally {
    loading.value = false
  }
}

const loadSupervisors = async () => {
  try {
    loading.value = true
    const response = await api.get('/api/authUser/supervisors/all')
    supervisors.value = response.data
  } catch (error) {
    console.error('Fehler beim Laden der Supervisors:', error)
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Laden der Supervisors'
    })
  } finally {
    loading.value = false
  }
}

const loadSupervisorsForApprentice = async () => {
  if (!selectedApprentice.value) {
    assignedSupervisors.value = []
    return
  }

  try {
    loading.value = true
    const response = await api.get(`/api/authUser/supervisors-for-apprentice`, {
      params: {
        apprenticeUsername: selectedApprentice.value.username
      }
    })
    assignedSupervisors.value = response.data
  } catch (error) {
    console.error('Fehler beim Laden der zugewiesenen Supervisors:', error)
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Laden der zugewiesenen Supervisors'
    })
  } finally {
    loading.value = false
  }
}

const assignSupervisor = async () => {
  if (!canAssignSupervisor.value) return

  try {
    loading.value = true
    await api.post('/api/authUser/set-supervisor', null, {
      params: {
        apprenticeUsername: selectedApprentice.value.username,
        supervisorUsername: selectedSupervisor.value.username
      }
    })

    Notify.create({
      type: 'positive',
      message: 'Supervisor erfolgreich zugewiesen'
    })

    // Supervisors neu laden
    await loadSupervisorsForApprentice()
    selectedSupervisor.value = null
  } catch (error) {
    console.error('Fehler beim Zuweisen des Supervisors:', error)
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Zuweisen des Supervisors'
    })
  } finally {
    loading.value = false
  }
}

const removeSupervisor = async (supervisor) => {
  if (!selectedApprentice.value) return

  try {
    loading.value = true
    await api.delete('/api/authUser/delete-connection', {
      params: {
        apprenticeUsername: selectedApprentice.value.username,
        supervisorUsername: supervisor.username
      }
    })

    Notify.create({
      type: 'positive',
      message: 'Supervisor-Zuweisung erfolgreich entfernt'
    })

    // Supervisors neu laden
    await loadSupervisorsForApprentice()
  } catch (error) {
    console.error('Fehler beim Entfernen des Supervisors:', error)
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Entfernen des Supervisors'
    })
  } finally {
    loading.value = false
  }
}

// Lifecycle Hooks
onMounted(async () => {
  // Prüfen, ob der Benutzer Admin-Rechte hat
  if (!authStore.isAdmin) {
    Notify.create({
      type: 'negative',
      message: 'Sie haben keine Berechtigung, auf diese Seite zuzugreifen'
    })
    return
  }

  await Promise.all([
    loadApprentices(),
    loadSupervisors()
  ])
})
</script>

<style scoped lang="scss">
.supervisor-card {
  height: 100%;
}
</style>
