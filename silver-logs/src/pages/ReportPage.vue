<template>
  <q-page class="q-pa-md column bg-grey-1">
    <div class="row">
      <!-- Sidebar mit Wochen -->
      <div class="col-2 bg-primary text-white">
        <q-scroll-area class="full-height" ref="scrollAreaRef">
          <div
            v-for="(week, index) in weeks"
            :key="index"
            :ref="el => (weekRefs[week.reportNumber] = el)"
            class="q-pa-md cursor-pointer week-item row no-wrap items-center"
            :class="{ 'bg-info': selectedReportNumber === week.reportNumber }"
            @click="selectWeek(week.reportNumber)"
          >
            <!-- Farbbalken links -->
            <div
              class="status-indicator"
              :style="{ backgroundColor: getStatusColor(week) }"
            >oi</div>

            <!-- Textinhalt -->
            <div class="q-ml-md">
              <div class="text-h6">{{ week.reportNumber }}.</div>
              <div>{{ week.weekStart }} - {{ week.weekEnd}}</div>
            </div>
          </div>
        </q-scroll-area>
      </div>

      <!-- Hauptinhalt -->
      <div class="col q-pa-md">
        <div class="row items-center justify-between q-mb-md">
          <div>
            <span class="text-subtitle1">Abteilung:</span>
            <q-select
              v-model="report.department"
              :options="departmentOptions"
              dense
              filled
              class="q-ml-sm"
              style="width: 200px"
              :disable="!isEditing"
            />
          </div>
          <q-btn color="primary" label="PDF" icon="picture_as_pdf" :disable="!report.reportNumber" />
        </div>

        <q-card class="q-pa-md">
          <q-card-section>
            <q-expansion-item label="Betriebliche Tätigkeiten" default-opened>
              <q-editor
                v-model="report.weekText"
                :readonly="!isEditing"
                :fonts="{
                  arial: 'Arial',
                  arial_black: 'Arial Black',
                  comic_sans: 'Comic Sans MS',
                  courier_new: 'Courier New',
                  impact: 'Impact',
                  lucida_grande: 'Lucida Grande',
                  times_new_roman: 'Times New Roman',
                  verdana: 'Verdana'
                }"
                :toolbar="isEditing ? editorToolbar : []"
                min-height="100px"
              />
            </q-expansion-item>

            <q-expansion-item label="Unterweisungen, betrieblicher Unterricht, sonstige Schulungen" default-opened>
              <q-editor
                v-model="report.instructionText"
                :readonly="!isEditing"
                :toolbar="isEditing ? editorToolbar : []"
                :fonts="{
                  arial: 'Arial',
                  arial_black: 'Arial Black',
                  comic_sans: 'Comic Sans MS',
                  courier_new: 'Courier New',
                  impact: 'Impact',
                  lucida_grande: 'Lucida Grande',
                  times_new_roman: 'Times New Roman',
                  verdana: 'Verdana'
                }"
                min-height="100px"
              />
            </q-expansion-item>

            <q-expansion-item label="Themen des Berufsschulunterrichts" default-opened>
              <q-editor
                v-model="report.schoolText"
                :readonly="!isEditing"
                :toolbar="isEditing ? editorToolbar : []"
                :fonts="{
                  arial: 'Arial',
                  arial_black: 'Arial Black',
                  comic_sans: 'Comic Sans MS',
                  courier_new: 'Courier New',
                  impact: 'Impact',
                  lucida_grande: 'Lucida Grande',
                  times_new_roman: 'Times New Roman',
                  verdana: 'Verdana'
                }"
                min-height="100px"
              />
            </q-expansion-item>

            <q-expansion-item label="Sonstiges" expand-separator>
              <q-editor
                v-model="report.extraText"
                :readonly="!isEditing"
                :toolbar="isEditing ? editorToolbar : []"
                :fonts="{
                  arial: 'Arial',
                  arial_black: 'Arial Black',
                  comic_sans: 'Comic Sans MS',
                  courier_new: 'Courier New',
                  impact: 'Impact',
                  lucida_grande: 'Lucida Grande',
                  times_new_roman: 'Times New Roman',
                  verdana: 'Verdana'
                }"
                min-height="100px"
              />
            </q-expansion-item>
          </q-card-section>
        </q-card>

        <div class="row q-mt-md justify-between items-center">
          <q-btn
            :color="isEditing ? 'negative' : 'primary'"
            :label="isEditing ? 'Abbrechen' : 'Bearbeiten'"
            :icon="isEditing ? 'close' : 'edit'"
            @click="toggleEdit"
          />
          <q-btn
            color="primary"
            label="Speichern"
            icon="save"
            @click="saveReport"
            :disable="!isEditing"
          />
          <q-btn
            color="positive"
            label="Abschicken"
            icon="send"
            @click="submitReport"
            :disable="isEditing || report.submitted"
          />
        </div>
      </div>

      <!-- Kommentarbox -->
      <div class="col-2 q-pa-md">
        <div class="text-subtitle1 q-mb-sm">Kommentar:</div>
        <q-input
          v-model="report.comment"
          type="textarea"
          filled
          autogrow
          :readonly="!isEditing && !isAdmin"
          style="height: 200px"
        />
        <div class="text-subtitle2 q-mt-md">
          Status:
          <q-badge :color="getStatusColor()" class="q-ml-sm">
            {{ getStatusText() }}
          </q-badge>
        </div>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import { date } from 'quasar';
import { api } from 'src/boot/axios';
import { useQuasar } from 'quasar';
import ReportDto from 'src/dtos/ReportDto.js';

// << HELPERS >>
const $q = useQuasar();

// << VARIABLES >>
const isEditing = ref(false);
const isAdmin = ref(false); // Hier sollte die tatsächliche Benutzerrolle eingesetzt werden
const weeks = ref();
const selectedReportNumber = ref(1);

const scrollAreaRef = ref(null)
const weekRefs = {} // Objekt, um jede Woche zu referenzieren
watch(selectedReportNumber, async (newVal) => {
  await nextTick()
  scrollToSelectedWeek(newVal)
})

function scrollToSelectedWeek(reportNumber) {
  const el = weekRefs[reportNumber]
  const scrollArea = scrollAreaRef.value?.getScrollTarget?.()
  if (el && scrollArea) {
    const elTop = el.offsetTop
    const elHeight = el.offsetHeight
    const scrollHeight = scrollArea.clientHeight

    const scrollPos = elTop - scrollHeight / 2 + elHeight / 2
    scrollArea.scrollTo({
      top: scrollPos,
      behavior: 'instant'
    })
  }
}
const departmentOptions = [
  'IT-Abteilung',
  'Verwaltung',
  'Marketing',
  'Produktion',
  'Entwicklung'
];

// Editor-Toolbar-Konfiguration
const editorToolbar = [
  ['left', 'center', 'right', 'justify'],
  ['bold', 'italic', 'strike', 'underline'],
  ['unordered', 'ordered', 'outdent', 'indent'],
  ['hr', 'fullscreen'],
  [
    {
      label: $q.lang.editor.fontSize,
      icon: $q.iconSet.editor.fontSize,
      fixedLabel: true,
      fixedIcon: true,
      list: 'no-icons',
      options: [
        'size-1',
        'size-2',
        'size-3',
        'size-4',
        'size-5',
        'size-6',
        'size-7'
      ]
    },
    {
      label: $q.lang.editor.defaultFont,
      icon: $q.iconSet.editor.font,
      fixedIcon: true,
      list: 'no-icons',
      options: [
        'default_font',
        'arial',
        'arial_black',
        'comic_sans',
        'courier_new',
        'impact',
        'lucida_grande',
        'times_new_roman',
        'verdana'
      ]
    },
    'removeFormat'
  ],
  ['undo', 'redo']
];

async function fetchDateRange() {
  api.get('/api/report/getAllBadges').then(async response => {
    if (response.data.length > 0) {
      weeks.value = response.data;

      await selectWeek(response.data.length);
      scrollToSelectedWeek(selectedReportNumber.value)
    }
  })
}

// Initialisiere mit einem leeren ReportDto
const report = ref(createEmptyReport());

// Funktion zum Erstellen eines leeren Reports mit Standardwerten
function createEmptyReport() {
  return new ReportDto(
    null,        // weekStart
    null,        // weekEnd
    null,   // reportNumber
    '',          // weekText
    '',          // instructionText
    '',          // schoolText
    '',          // extraText
    '',          // department
    false,       // submitted
    false,       // approved
    false,       // rejected
    ''           // comment
  );
}

// Bericht laden für ausgewählte Woche
async function selectWeek(reportNumber) {
  const selectedWeek = weeks.value[reportNumber - 1];
  selectedReportNumber.value = reportNumber;
  try {
    // Versuchen Bericht zu laden
    const response = await api.get(`/api/report/${selectedWeek.weekStart}`);

    // Verwende die fromObject Methode des DTO
    report.value = ReportDto.fromObject(response.data);

    // Stelle sicher, dass null-Textwerte als leere Strings gesetzt werden
    if (report.value.weekText === null) report.value.weekText = '';
    if (report.value.instructionText === null) report.value.instructionText = '';
    if (report.value.schoolText === null) report.value.schoolText = '';
    if (report.value.extraText === null) report.value.extraText = '';
    if (report.value.comment === null) report.value.comment = '';
    if (report.value.department === null) report.value.department = '';

  } catch (error) {
    console.log(error)
    // Wenn kein Bericht gefunden, erstelle einen neuen mit den Daten der ausgewählten Woche
    report.value = new ReportDto(
      date.formatDate(selectedWeek.startDate, 'YYYY-MM-DD'),
      date.formatDate(selectedWeek.endDate, 'YYYY-MM-DD'),
      null,          // reportNumber
      '',               // weekText
      '',          // instructionText
      '',          // schoolText
      '',          // extraText
      '',          // department
      false,       // submitted
      false,       // approved
      false,       // rejected
      ''           // comment
    );

    $q.notify({
      message: 'Kein Bericht für diese Woche gefunden. Neuer Bericht wird vorbereitet.',
      color: 'info'
    });
  }

  // Bearbeitungsmodus deaktivieren
  isEditing.value = false;
};

// Bearbeitungsmodus umschalten
const toggleEdit = () => {
  if (isEditing.value && report.value.reportNumber) {
    // Wenn Bearbeitung abgebrochen wird und Bericht existiert, lade Originalversion
    selectWeek(selectedReportNumber.value);
  } else {
    isEditing.value = !isEditing.value;
  }
};

// Bericht speichern
const saveReport = async () => {
  try {
    const response = await api.post('/api/report/save', report.value);

    // Verwende die fromObject Methode des DTO für die Antwortdaten
    report.value = ReportDto.fromObject(response.data);

    // Stelle sicher, dass null-Textwerte als leere Strings gesetzt werden
    if (report.value.weekText === null) report.value.weekText = '';
    if (report.value.instructionText === null) report.value.instructionText = '';
    if (report.value.schoolText === null) report.value.schoolText = '';
    if (report.value.extraText === null) report.value.extraText = '';
    if (report.value.comment === null) report.value.comment = '';
    if (report.value.department === null) report.value.department = '';

    $q.notify({
      message: 'Bericht erfolgreich gespeichert',
      color: 'positive'
    });

    isEditing.value = false;
  } catch (error) {
    $q.notify({
      message: 'Fehler beim Speichern des Berichts',
      color: 'negative'
    });
    console.error(error);
  }
};

// Bericht einreichen
const submitReport = async () => {
  try {
    report.value.submitted = true;
    const response = await api.post('/api/report/save', report.value);

    // Verwende die fromObject Methode des DTO für die Antwortdaten
    report.value = ReportDto.fromObject(response.data);

    // Stelle sicher, dass null-Textwerte als leere Strings gesetzt werden
    if (report.value.weekText === null) report.value.weekText = '';
    if (report.value.instructionText === null) report.value.instructionText = '';
    if (report.value.schoolText === null) report.value.schoolText = '';
    if (report.value.extraText === null) report.value.extraText = '';
    if (report.value.comment === null) report.value.comment = '';
    if (report.value.department === null) report.value.department = '';

    $q.notify({
      message: 'Bericht erfolgreich eingereicht',
      color: 'positive'
    });
  } catch (error) {
    $q.notify({
      message: 'Fehler beim Einreichen des Berichts',
      color: 'negative'
    });
    console.error(error);
  }
};

// Status-Anzeige
function getStatusColor(week) {
  if (week) {
    if (week.approved) return 'green';
    if (week.rejected) return 'red';
    if (week.submitted) return 'yellow';
    return 'grey';
  }
  if (report.value.approved) return 'green';
  if (report.value.rejected) return 'red';
  if (report.value.submitted) return 'yellow';
  return 'grey';
}

const getStatusText = () => {
  if (report.value.approved) return 'Genehmigt';
  if (report.value.rejected) return 'Abgelehnt';
  if (report.value.submitted) return 'Eingereicht';
  return 'Ausstehend';
};

// Initialisierung
onMounted(async() => {
  await fetchDateRange();
});
</script>

<style scoped>
.week-item {
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  transition: background-color 0.3s;
}
.week-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}
.bg-blue {
  background-color: #1976D2 !important;
}
</style>
