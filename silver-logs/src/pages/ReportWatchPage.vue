<template>
  <q-page padding>
    <div class="q-pa-md">
      <div class="text-h5 q-mr-auto q-mb-md">Berichte Übersicht</div>

      <div class="q-mb-md">
        <q-select
          v-model="selectedApprentice"
          :options="apprenticeOptions"
          option-label="username"
          label="Auszubildenden auswählen"
          emit-value
          map-options
          clearable
          style="width: 250px"
          @update:model-value="loadReportBadges"
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
      </div>

      <q-table
        :rows="reports"
        :columns="columns"
        :row-key="row => row.reportNumber"
        :loading="loading"
        :pagination="pagination"
      >
        <template v-slot:body="props">
          <q-tr :props="props" @click="viewReport(props.row.weekStart)">
            <q-td key="reportNumber" :props="props">{{ props.row.reportNumber }}</q-td>
            <q-td key="week" :props="props">{{ props.row.weekStart }} - {{ props.row.weekEnd}}</q-td>
            <q-td key="status" :props="props">
              <q-chip :color="props.row.statusColor" text-color="white">
                {{ props.row.statusText }}
              </q-chip>
            </q-td>
          </q-tr>
        </template>

        <template v-slot:no-data>
          <div class="full-width text-center q-pa-md">
            Keine Berichte gefunden
          </div>
        </template>
      </q-table>

      <!-- Loading Overlay -->
      <q-inner-loading :showing="loading">
        <q-spinner size="50px" color="primary" />
      </q-inner-loading>

      <!-- Report Detail Dialog -->
      <q-dialog v-model="showReportDialog" maximized>
        <q-card>
          <q-card-section class="row items-center q-pb-none">
            <div class="text-h6">
              Berichtnummer {{ selectedReport?.reportNumber }}
            </div>
            <q-space />
            <q-btn icon="close" flat round dense v-close-popup />
          </q-card-section>

          <q-card-section v-if="selectedReport">
            <div class="row q-gutter-md">
              <div class="col-12 col-md-6">
                <q-card flat bordered>
                  <q-card-section>
                    <div class="text-subtitle2">Betriebliche Tätigkeiten</div>
                    <div v-html="selectedReport.weekText" />
                  </q-card-section>
                </q-card>
              </div>

              <div class="col-12 col-md-6">
                <q-card flat bordered>
                  <q-card-section>
                    <div class="text-subtitle2">Unterweisungen</div>
                    <div class="q-mt-sm" v-html="selectedReport.instructionText"/>
                  </q-card-section>
                </q-card>
              </div>

              <div class="col-12 col-md-6">
                <q-card flat bordered>
                  <q-card-section>
                    <div class="text-subtitle2">Berufsschule</div>
                    <div class="q-mt-sm" v-html="selectedReport.schoolText"/>
                  </q-card-section>
                </q-card>
              </div>

              <div class="col-12 col-md-6">
                <q-card flat bordered>
                  <q-card-section>
                    <div class="text-subtitle2">Sonstiges</div>
                    <div class="q-mt-sm" v-html="selectedReport.extraText"/>
                  </q-card-section>
                </q-card>
              </div>
            </div>

            <!-- Comment Section -->
            <div class="q-mt-md" v-if="selectedReport.submitted && !selectedReport.approved && !selectedReport.rejected">
              <q-input
                v-model="comment"
                type="textarea"
                label="Kommentar (optional)"
                rows="3"
                outlined
              />

              <div class="row q-gutter-sm q-mt-md">
                <q-btn
                  @click="approveReport(selectedReport)"
                  color="positive"
                  label="Genehmigen"
                  icon="check"
                />
                <q-btn
                  @click="rejectReport(selectedReport)"
                  color="negative"
                  label="Ablehnen"
                  icon="close"
                />
              </div>
            </div>

            <div v-if="selectedReport.comment" class="q-mt-md">
              <q-card flat bordered>
                <q-card-section>
                  <div class="text-subtitle2">Kommentar</div>
                  <div class="q-mt-sm">{{ selectedReport.comment }}</div>
                </q-card-section>
              </q-card>
            </div>
          </q-card-section>
        </q-card>
      </q-dialog>
    </div>
  </q-page>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { api } from 'boot/axios.js';
import { Notify } from 'quasar';

// << HELPER >>
const loading = ref(false);

// << VARIABLES >>
const reports = ref([]);
const apprenticeOptions = ref([]);
const selectedApprentice = ref();
const selectedReport = ref();
const showReportDialog = ref(false);
const comment = ref('');

const columns = [
  { name: 'reportNumber', label: 'Bericht Nr.', field: 'reportNumber', align: 'left', sortable: true },
  { name: 'week', label: 'Datum', field: row => `${row.weekStart} - ${row.weekEnd}`, align: 'left' },
  { name: 'status', label: 'Status', field: 'statusText', align: 'left', sortable: true },
]

const pagination = ref({
  rowsPerPage: 10,
  sortBy: 'reportNumber',
  descending: true
})
// << FUNCTIONS >>
function getStatusColor(row) {
  if (row.approved) return 'positive';
  if (row.rejected) return 'negative';
  if (row.submitted) return 'warning';
  return 'grey';
}

function getStatusText(row) {
  if (row.approved) return 'Genehmigt';
  if (row.rejected) return 'Abgelehnt';
  if (row.submitted) return 'Eingereicht';
  return 'Ausstehend';
}

async function viewReport(weekStart) {
  try {
    loading.value = true
    const response = await api.get(`/api/report/apprentice/${weekStart}/${selectedApprentice.value.username}`)
    selectedReport.value = response.data
    comment.value = selectedReport.value.comment || ''
  } catch (error) {
    console.error('Fehler beim Laden des Berichts:', error)
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Laden des Berichts'
    })
  } finally {
    loading.value = false
    showReportDialog.value = true;
  }
}

async function loadApprentices() {
  try {
    loading.value = true
    const response = await api.get('/api/authUser/apprentice-for-supervisors')
    apprenticeOptions.value = response.data
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

async function loadReportBadges() {
  if (!selectedApprentice.value) {
    reports.value = []
    return
  }
  try {
    loading.value = true
    const response = await api.get(`/api/report/apprentice/getAllBadges/${selectedApprentice.value.username}`)
    reports.value = response.data.map(report => {
      return {
        ...report,
        statusText: getStatusText(report),
        statusColor: getStatusColor(report),
      }
    })
  } catch (error) {
    console.error('Fehler beim Laden der Berichte:', error)
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Laden der Berichte'
    })
  } finally {
    loading.value = false
  }
}

async function approveReport(report) {
  try {
    loading.value = true
    const response = await api.post(`/api/report/approve/${report.weekStart}/${selectedApprentice.value.username}`, {
      comment: comment.value
    })
    console.log(response)
    Notify.create({
      type: 'positive',
      message: 'Bericht genehmigt'
    })
  } catch (error) {
    console.error('Fehler beim Genehmigen des Berichts:', error)
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Genehmigen des Berichts'
    })
  } finally {
    await loadReportBadges()
    showReportDialog.value = false
    loading.value = false
  }
}

async function rejectReport(report) {
  try {
    loading.value = true
    const response = await api.post(`/api/report/reject/${report.weekStart}/${selectedApprentice.value.username}`, {
      comment: comment.value
    })
    console.log(response)
    Notify.create({
      type: 'positive',
      message: 'Bericht abgelehnt'
    })
  } catch (error) {
    console.error('Fehler beim Ablehnen des Berichts:', error)
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Ablehnen des Berichts'
    })
  } finally {
    await loadReportBadges()
    showReportDialog.value = false
    loading.value = false
  }
}
// << LIFECYCLE HOOKS >>
onMounted(async () => {
  await loadApprentices()
})
</script>

<style scoped>

</style>
