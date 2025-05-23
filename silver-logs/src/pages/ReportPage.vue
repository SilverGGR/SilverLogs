<template>
  <q-page class="q-pa-md column bg-grey-1">
    <div class="row">
      <!-- Sidebar mit Wochen -->
      <div class="col-2 bg-primary text-white">
        <div
          v-for="(week, index) in weeks"
          :key="index"
          class="q-pa-md cursor-pointer week-item"
          :class="{ 'bg-blue': selectedWeekIndex === index }"
          @click="selectWeek(index)"
        >
          <div class="text-h6">{{ index + 1 }}.</div>
          <div>{{ week.text }}</div>
        </div>
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
        <div v-if="report.submitted" class="text-subtitle2 q-mt-md">
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
import { ref, onMounted } from 'vue';
import { date } from 'quasar';
import { api } from 'src/boot/axios';
import { useQuasar } from 'quasar';
import ReportDto from 'src/dtos/ReportDto.js';

// << HELPERS >>
const $q = useQuasar();

// << VARIABLES >>
const isEditing = ref(false);
const isAdmin = ref(false); // Hier sollte die tatsächliche Benutzerrolle eingesetzt werden
const selectedWeekIndex = ref(0);
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

// Wochen generieren
const generateWeeks = () => {
  const weekArray = [];
  const today = new Date();
  const currentMonday = new Date(today);
  const dayOfWeek = today.getDay();

  // Setze auf vorherigen Montag
  currentMonday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));

  // Generiere 12 Wochen zurück
  for (let i = 0; i < 12; i++) {
    const weekStartDate = new Date(currentMonday);
    weekStartDate.setDate(weekStartDate.getDate() - (i * 7));

    const weekEndDate = new Date(weekStartDate);
    weekEndDate.setDate(weekStartDate.getDate() + 6);

    weekArray.push({
      startDate: weekStartDate,
      endDate: weekEndDate,
      text: `${date.formatDate(weekStartDate, 'DD.MM.YYYY')} – ${date.formatDate(weekEndDate, 'DD.MM.YYYY')}`
    });
  }

  return weekArray;
};

const weeks = ref(generateWeeks());

// Initialisiere mit einem leeren ReportDto
const report = ref(createEmptyReport());

// Funktion zum Erstellen eines leeren Reports mit Standardwerten
function createEmptyReport() {
  return new ReportDto(
    null,        // weekStart
    null,        // weekEnd
    '',          // weekText
    '',          // instructionText
    '',          // schoolText
    '',          // extraText
    '',          // department
    null,        // reportNumber
    false,       // submitted
    false,       // approved
    false,       // rejected
    ''           // comment
  );
}

// Bericht laden für ausgewählte Woche
const selectWeek = async (index) => {
  selectedWeekIndex.value = index;
  const selectedWeek = weeks.value[index];

  try {
    // Versuchen Bericht zu laden
    const weekStartStr = date.formatDate(selectedWeek.startDate, 'YYYY-MM-DD');
    const response = await api.get(`/api/report/${weekStartStr}`);

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
      '',          // weekText
      '',          // instructionText
      '',          // schoolText
      '',          // extraText
      '',          // department
      null,        // reportNumber
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
    selectWeek(selectedWeekIndex.value);
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
const getStatusColor = () => {
  if (report.value.approved) return 'positive';
  if (report.value.rejected) return 'negative';
  return 'warning';
};

const getStatusText = () => {
  if (report.value.approved) return 'Genehmigt';
  if (report.value.rejected) return 'Abgelehnt';
  return 'Ausstehend';
};

// Initialisierung
onMounted(() => {
  // Lade ersten Bericht
  selectWeek(0);
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
