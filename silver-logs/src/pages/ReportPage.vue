<template>
  <q-page class="q-pa-md column bg-grey-1">
    <div class="row">
      <!-- Sidebar mit Wochen -->
      <div class="col-2 bg-primary text-white">
        <!-- Suchfelder -->
        <div class="q-pa-sm column gap-sm">
          <!-- Berichtnummer -->
          <q-input
            dense
            debounce="200"
            filled
            v-model="reportSearch"
            placeholder="Berichtnummer"
            type="number"
            class="bg-white text-black"
            clearable
          >
            <template #append>
              <q-icon name="search" />
            </template>
          </q-input>

          <!-- Datumsauswahl -->
          <q-input
            dense
            filled
            v-model="dateSearch"
            placeholder="Datum wählen"
            class="bg-white text-black"
            clearable
          >
            <template #append>
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-date v-model="dateSearch" mask="YYYY-MM-DD" first-day-of-week="1" today-btn/>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>

        <!-- Scrollbare Liste -->
        <q-scroll-area :style="{ height: 'calc(100vh - 200px)' }" ref="scrollAreaRef">
          <div
            v-for="(week, index) in filteredWeeks"
            :key="index"
            :ref="el => (weekRefs[week.reportNumber] = el)"
            class="q-pa-md cursor-pointer week-item row no-wrap items-center"
            :class="{ 'bg-info': selectedReportNumber === week.reportNumber }"
            :style="{ borderLeft: '20px solid ' + getStatusColor(week), paddingLeft: 0 }"
            @click="selectWeek(week.reportNumber)"
          >
            <div class="q-ml-md">
              <div class="text-h6">{{ week.reportNumber }}.</div>
              <div>{{ week.weekStart }} - {{ week.weekEnd }}</div>
            </div>
          </div>
        </q-scroll-area>
      </div>

      <!-- Hauptinhalt -->
      <div class="col q-px-md">
        <q-card class="q-pa-md">
          <div style="display: flex; justify-content: flex-start; gap: 10px">
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
            <q-btn color="primary" label="PDF" icon="picture_as_pdf" :disable="!report.reportNumber" />
            <q-btn
              color="positive"
              label="Abschicken"
              icon="send"
              @click="submitReport"
              :disable="isEditing || report.submitted || report.approved"
            />
          </div>
          <q-card-section>
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
      </div>

      <!-- Kommentarbox -->
      <div class="col-2 q-pa-md">
        <div class="text-subtitle2 q-mt-md">
          Status:
          <q-badge :color="getStatusColor()" class="q-ml-sm">
            {{ getStatusText() }}
          </q-badge>
        </div>
        <div class="text-subtitle1 q-mb-sm">Kommentar:</div>
        <q-input
          v-model="report.comment"
          type="textarea"
          filled
          autogrow
          :readonly="!isEditing && !isAdmin"
          style="height: 200px"
        />
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { date } from 'quasar';
import { api } from 'src/boot/axios';
import { useQuasar } from 'quasar';
import ReportDto from 'src/dtos/ReportDto.js';

// << HELPERS >>
const $q = useQuasar();

// << VARIABLES >>
const isEditing = ref(false);
const isAdmin = ref(false); // Hier sollte die tatsächliche Benutzerrolle eingesetzt werden
const weeks = ref([]);
const selectedReportNumber = ref(1);
const scrollAreaRef = ref(null)
const weekRefs = {} // Objekt, um jede Woche zu referenzieren

const reportSearch = ref('')
const dateSearch = ref(null) // format: YYYY-MM-DD

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
    if (week.approved) return '#21BA45';
    if (week.rejected) return '#C10015';
    if (week.submitted) return '#F2C037';
    return '#C0C0C0';
  }
  if (report.value.approved) return 'positive';
  if (report.value.rejected) return 'negative';
  if (report.value.submitted) return 'warning';
  return 'grey';
}

const getStatusText = () => {
  if (report.value.approved) return 'Genehmigt';
  if (report.value.rejected) return 'Abgelehnt';
  if (report.value.submitted) return 'Eingereicht';
  return 'Ausstehend';
};

const filteredWeeks = computed(() => {
  return weeks.value.filter(week => {
    const matchReport =
      !reportSearch.value || week.reportNumber.toString() === reportSearch.value

    const matchDate =
      !dateSearch.value ||
      (dateSearch.value >= week.weekStart && dateSearch.value <= week.weekEnd)

    return matchReport && matchDate
  })
})

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
