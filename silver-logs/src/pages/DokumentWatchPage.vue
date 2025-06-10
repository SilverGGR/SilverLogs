<template>
  <q-page padding>
    <div class="q-pa-md">
      <div class="text-h5 q-mr-auto q-mb-md">Dokumente Übersicht</div>

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
          @update:model-value="loadDocuments"
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

      <div class="q-mb-md">
        <q-input
          v-model="searchTerm"
          placeholder="Nach Dokumentname suchen..."
          debounce="300"
          clearable
          style="width: 300px"
        >
          <template v-slot:append>
            <q-icon name="search" />
          </template>
        </q-input>
      </div>

      <q-table
        :rows="filteredDocuments"
        :columns="columns"
        :row-key="row => row.id"
        :loading="loading"
        :pagination="pagination"
      >
        <template v-slot:body="props">
          <q-tr :props="props">
            <q-td key="fileName" :props="props">
              <q-icon
                :name="getFileIcon(props.row.fileName)"
                size="sm"
                class="q-mr-sm"
              />
              {{ props.row.fileName }}
            </q-td>
            <q-td key="actions" :props="props">
              <q-btn
                icon="download"
                color="primary"
                flat
                dense
                @click="downloadDocument(props.row)"
                title="Dokument herunterladen"
              />
              <q-btn
                icon="visibility"
                color="secondary"
                flat
                dense
                @click="previewDocument(props.row)"
                title="Dokument anzeigen"
                :disable="!canPreview(props.row.fileName)"
              />
              <q-btn
                icon="delete"
                color="negative"
                flat
                dense
                v-if="authStore.isAdmin"
                @click="confirmDelete(props.row)"
                title="Dokument löschen"
              />
            </q-td>
          </q-tr>
        </template>

        <template v-slot:no-data>
          <div class="full-width text-center q-pa-md">
            <q-icon name="description" size="3em" color="grey-5" />
            <div class="text-h6 q-mt-md">Keine Dokumente gefunden</div>
          </div>
        </template>
      </q-table>

      <!-- Loading Overlay -->
      <q-inner-loading :showing="loading">
        <q-spinner size="50px" color="primary" />
      </q-inner-loading>

      <!-- Document Preview Dialog -->
      <q-dialog v-model="showPreviewDialog" maximized>
        <q-card>
          <q-card-section class="row items-center q-pb-none">
            <div class="text-h6">{{ selectedDocument?.fileName }}</div>
            <q-space />
            <q-btn icon="download" flat round dense @click="downloadDocument(selectedDocument)" />
            <q-btn icon="close" flat round dense v-close-popup />
          </q-card-section>

          <q-card-section class="q-pt-none" style="height: calc(100vh - 120px);">
            <div class="full-height">
              <iframe
                v-if="previewUrl && canPreview(selectedDocument?.fileName)"
                :src="previewUrl"
                width="100%"
                height="100%"
                style="border: none;"
              />
              <div v-else class="text-center q-pa-xl">
                <q-icon name="error" size="4em" color="grey-5" />
                <div class="text-h6 q-mt-md">Vorschau nicht verfügbar</div>
                <div class="text-body2">Bitte laden Sie das Dokument herunter</div>
              </div>
            </div>
          </q-card-section>
        </q-card>
      </q-dialog>
    </div>
  </q-page>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { api } from 'boot/axios.js';
import { Notify, useQuasar } from 'quasar';
import { useAuthStore } from 'stores/auth.js';

// << HELPER >>
const loading = ref(false);
const authStore = useAuthStore();
const $q = useQuasar();

// << VARIABLES >>
const documents = ref([]);
const apprenticeOptions = ref([]);
const selectedApprentice = ref();
const selectedDocument = ref();
const showPreviewDialog = ref(false);
const previewUrl = ref('');
const searchTerm = ref('');

const columns = [
  { name: 'fileName', label: 'Dateiname', field: 'fileName', align: 'left', sortable: true },
  { name: 'actions', label: 'Aktionen', field: '', align: 'center' },
];

const pagination = ref({
  rowsPerPage: 10,
  sortBy: 'fileName',
  descending: true
});

// << FUNCTIONS >>
function getFileIcon(fileName) {
  const extension = fileName.split('.').pop().toLowerCase();
  switch (extension) {
    case 'pdf': return 'picture_as_pdf';
    case 'doc':
    case 'docx': return 'description';
    case 'xls':
    case 'xlsx': return 'table_chart';
    case 'jpg':
    case 'jpeg':
    case 'png':
    case 'gif': return 'image';
    case 'txt': return 'text_snippet';
    default: return 'attachment';
  }
}

function canPreview(fileName) {
  const extension = fileName.split('.').pop().toLowerCase();
  return ['pdf', 'txt', 'jpg', 'jpeg', 'png', 'gif'].includes(extension);
}

async function loadApprentices() {
  try {
    loading.value = true;
    const response = await api.get('/api/authUser/apprentice-for-supervisors');
    apprenticeOptions.value = response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Auszubildenden:', error);
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Laden der Auszubildenden'
    });
  } finally {
    loading.value = false;
  }
}

async function loadDocuments() {
  if (!selectedApprentice.value) {
    documents.value = [];
    return;
  }

  try {
    loading.value = true;
    const response = await api.get(`/api/document/forUser/getAllBadges/${selectedApprentice.value.username}`);
    documents.value = response.data;
  } catch (error) {
    console.error('Fehler beim Laden der Dokumente:', error);
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Laden der Dokumente'
    });
  } finally {
    loading.value = false;
  }
}

async function previewDocument(doc) {
  if (!canPreview(doc.fileName)) {
    Notify.create({
      type: 'warning',
      message: 'Vorschau für diesen Dateityp nicht verfügbar'
    });
    return;
  }

  try {
    selectedDocument.value = doc;
    const response = await api.get(`/api/document/preview/${doc.id}`, {
      responseType: 'blob'
    });

    previewUrl.value = window.URL.createObjectURL(response.data);
    showPreviewDialog.value = true;
  } catch (error) {
    console.error('Fehler beim Laden der Vorschau:', error);
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Laden der Vorschau'
    });
  }
}

async function downloadDocument(doc) {
  try {
    const response = await api.get(`/api/document/download/${doc.id}`, {
      responseType: 'blob'
    });

    const url = window.URL.createObjectURL(response.data);
    const link = window.document.createElement('a'); // Explizit window.document verwenden
    link.href = url;
    link.setAttribute('download', doc.fileName);
    window.document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);

    Notify.create({
      type: 'positive',
      message: 'Dokument erfolgreich heruntergeladen'
    });
  } catch (error) {
    console.error('Fehler beim Herunterladen:', error);
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Herunterladen des Dokuments'
    });
  }
}

function confirmDelete(doc) {
  $q.dialog({
    title: 'Dokument löschen',
    message: `Möchten Sie das Dokument "${doc.fileName}" wirklich löschen?`,
    cancel: true,
    persistent: true
  }).onOk(() => {
    deleteDocument(doc);
  });
}

async function deleteDocument(doc) {
  try {
    loading.value = true;
    await api.delete(`/api/document/${doc.id}`);
    await loadDocuments();

    Notify.create({
      type: 'positive',
      message: 'Dokument erfolgreich gelöscht'
    });
  } catch (error) {
    console.error('Fehler beim Löschen:', error);
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Löschen des Dokuments'
    });
  } finally {
    loading.value = false;
  }
}
// << COMPUTED >>
const filteredDocuments = computed(() => {
  if (!searchTerm.value) {
    return documents.value;
  }
  return documents.value.filter(doc =>
    doc.fileName.toLowerCase().includes(searchTerm.value.toLowerCase())
  );
});

// << LIFECYCLE HOOKS >>
onMounted(async () => {
  await loadApprentices();
});
</script>

<style scoped>
</style>
