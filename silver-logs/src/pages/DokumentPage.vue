<template>
  <q-page padding>
    <div class="q-pa-md">
      <div class="text-h5 q-mr-auto q-mb-md">Meine Dokumente</div>

      <!-- Upload Bereich -->
      <q-card class="q-mb-lg">
        <q-card-section>
          <div class="text-h6 q-mb-md">Dokument hochladen</div>

          <q-file
            v-model="selectedFile"
            label="Datei auswählen"
            outlined
            :max-file-size="maxFileSize"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.gif,.txt"
            @rejected="onRejected"
          >
            <template v-slot:prepend>
              <q-icon name="attach_file" />
            </template>
          </q-file>

          <div class="q-mt-md">
            <q-btn
              color="primary"
              icon="upload"
              label="Hochladen"
              @click="uploadDocument"
              :disable="!selectedFile || uploading"
              :loading="uploading"
            />
          </div>

          <div class="text-caption q-mt-sm text-grey-6">
            Erlaubte Dateitypen: PDF, Word, Excel, Bilder, Text<br>
            Maximale Dateigröße: {{ formatFileSize(maxFileSize) }}
          </div>
        </q-card-section>
      </q-card>

      <!-- Suchfeld -->
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

      <!-- Dokumente Tabelle -->
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
                @click="confirmDelete(props.row)"
                title="Dokument löschen"
              />
            </q-td>
          </q-tr>
        </template>

        <template v-slot:no-data>
          <div class="full-width text-center q-pa-md">
            <q-icon name="description" size="3em" color="grey-5" />
            <div class="text-h6 q-mt-md">Keine Dokumente vorhanden</div>
            <div class="text-body2">Laden Sie Ihr erstes Dokument hoch</div>
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
import { Notify } from 'quasar';
import { useQuasar } from 'quasar';

// << HELPER >>
const $q = useQuasar();
const loading = ref(false);
const uploading = ref(false);

// << VARIABLES >>
const documents = ref([]);
const selectedFile = ref(null);
const selectedDocument = ref();
const showPreviewDialog = ref(false);
const previewUrl = ref('');
const searchTerm = ref('');
const maxFileSize = 10 * 1024 * 1024; // 10MB

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

function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function onRejected(rejectedEntries) {
  console.log(rejectedEntries)
  Notify.create({
    type: 'negative',
    message: `Datei zu groß. Maximum: ${formatFileSize(maxFileSize)}`
  });
}

async function uploadDocument() {
  if (!selectedFile.value) return;

  try {
    uploading.value = true;
    const formData = new FormData();
    formData.append('file', selectedFile.value);

    await api.post('/api/document/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    selectedFile.value = null;
    await loadDocuments();

    Notify.create({
      type: 'positive',
      message: 'Dokument erfolgreich hochgeladen'
    });
  } catch (error) {
    console.error('Fehler beim Hochladen:', error);
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Hochladen des Dokuments'
    });
  } finally {
    uploading.value = false;
  }
}

async function loadDocuments() {
  try {
    loading.value = true;
    const response = await api.get('/api/document/getAllBadges');
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
  await loadDocuments();
});
</script>

<style scoped>
</style>
