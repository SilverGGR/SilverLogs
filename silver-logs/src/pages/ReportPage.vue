<template>
  <q-page class="q-pa-md column bg-grey-1">
    <div class="row">
      <!-- Sidebar mit Wochen -->
      <div class="col-2 shadow-4 text-white">
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
            :style="{
              borderLeft: '20px solid ' + getStatusColor(week), paddingLeft: 0,
              backgroundColor: selectedReportNumber === week.reportNumber ? getStatusColor(week) : 'white',
              color: selectedReportNumber === week.reportNumber ? 'white' : 'black'
            }"
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
          <div style="display: flex; justify-content: flex-start; gap: 16px">
            <q-btn
              :color="isEditing ? 'negative' : 'primary'"
              :icon="isEditing ? 'close' : 'edit'"
              :disable="!report.rejected && (report.submitted || report.approved)"
              @click="toggleEdit"
            />
            <q-btn
              color="primary"
              icon="save"
              @click="saveReport"
              :disable="!isEditing"
            />
            <q-btn-dropdown
              color="primary"
              icon="picture_as_pdf"
              :disable="!report.reportNumber && weeks.length === 0"
            >
              <q-list style="min-width: 150px;">
                <q-item clickable @click="generatePDFWithFormatting">
                  <q-item-section>Aktueller Bericht</q-item-section>
                </q-item>

                <q-item clickable @click="generateAllReportsPDF">
                  <q-item-section>Alle Berichte</q-item-section>
                </q-item>

                <q-item clickable @click="showMultiSelectDialog">
                  <q-item-section>Auswahl</q-item-section>
                </q-item>
              </q-list>
            </q-btn-dropdown>

            <q-btn
              color="positive"
              icon="send"
              @click="submitReport"
              :disable="isEditing || !report.rejected && (report.submitted || report.approved)"
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
                @drop.prevent
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
          :readonly="true"
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
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { useAuthStore } from 'stores/auth.js';

// << HELPERS >>
const $q = useQuasar();
const authStore = useAuthStore();

// << VARIABLES >>
const isEditing = ref(false);
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

async function fetchDateRange(scroll) {
  api.get('/api/report/getAllBadges').then(async response => {
    if (response.data.length > 0) {
      weeks.value = response.data;

      if (scroll) {
        await selectWeek(response.data.length -1);
        scrollToSelectedWeek(selectedReportNumber.value)
      }
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
}

// Bearbeitungsmodus umschalten
function toggleEdit() {
  if (isEditing.value && report.value.reportNumber) {
    // Zeige Bestätigungsdialog
    $q.dialog({
      title: 'Bearbeitung abbrechen',
      message: 'Möchten Sie die Bearbeitung wirklich abbrechen? Alle ungespeicherten Änderungen gehen verloren.',
      cancel: {
        label: 'Abbrechen',
        color: 'grey',
        flat: true
      },
      ok: {
        label: 'OK',
        color: 'negative'
      },
      persistent: true
    }).onOk(() => {
      // Bei OK: Originalversion laden und Bearbeitungsmodus beenden
      selectWeek(selectedReportNumber.value);
    }).onCancel(() => {
      // Bei Abbrechen: Nichts tun, Dialog schließt sich automatisch
    });
  } else {
    isEditing.value = !isEditing.value;
  }
}

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
    report.value.rejected = false;
    const response = await api.post('/api/report/save', report.value);
    await fetchDateRange(false)
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
    return '#c0c0c0';
  }
  if (report.value.approved) return 'positive';
  if (report.value.rejected) return 'negative';
  if (report.value.submitted) return 'warning';
  return 'grey';
}

function getStatusText() {
  if (report.value.approved) return 'Genehmigt';
  if (report.value.rejected) return 'Abgelehnt';
  if (report.value.submitted) return 'Eingereicht';
  return 'Ausstehend';
}

// Hilfsfunktion zum Verarbeiten des HTML-Inhalts und korrigieren der Listen
function processHtmlContent(htmlContent) {
  if (!htmlContent) return 'Keine Angaben';

  // Erstelle ein temporäres DOM-Element zum Parsen
  const tempDiv = document.createElement('div');
  tempDiv.innerHTML = htmlContent;

  // Verarbeite ungeordnete Listen (ul/li)
  const ulElements = tempDiv.querySelectorAll('ul');
  ulElements.forEach(ul => {
    const listItems = ul.querySelectorAll('li');
    listItems.forEach(li => {
      // Füge Bullet-Point vor jedem Listenelement hinzu
      li.innerHTML = `• ${li.innerHTML}`;
      li.style.display = 'block';
      li.style.marginLeft = '20px';
      li.style.marginBottom = '5px';
    });
    // Wandle ul in div um
    const div = document.createElement('div');
    div.innerHTML = ul.innerHTML;
    div.style.marginBottom = '10px';
    ul.parentNode.replaceChild(div, ul);
  });

  // Verarbeite geordnete Listen (ol/li)
  const olElements = tempDiv.querySelectorAll('ol');
  olElements.forEach(ol => {
    const listItems = ol.querySelectorAll('li');
    listItems.forEach((li, index) => {
      // Füge Nummer vor jedem Listenelement hinzu
      li.innerHTML = `${index + 1}. ${li.innerHTML}`;
      li.style.display = 'block';
      li.style.marginLeft = '20px';
      li.style.marginBottom = '5px';
    });
    // Wandle ol in div um
    const div = document.createElement('div');
    div.innerHTML = ol.innerHTML;
    div.style.marginBottom = '10px';
    ol.parentNode.replaceChild(div, ol);
  });

  // Verarbeite Absätze und Zeilenumbrüche
  const pElements = tempDiv.querySelectorAll('p');
  pElements.forEach(p => {
    if (p.innerHTML.trim() === '' || p.innerHTML === '<br>') {
      p.style.height = '10px';
      p.innerHTML = '&nbsp;';
    }
    p.style.marginBottom = '10px';
  });

  // Ersetze <br> Tags mit echten Zeilenumbrüchen
  tempDiv.innerHTML = tempDiv.innerHTML.replace(/<br\s*\/?>/gi, '<br style="line-height: 1.5;">');

  return tempDiv.innerHTML;
}

// Vereinheitlichte PDF-Generierung mit Multi-Page Support
async function generatePDFForReport(reportData, pdf = null) {
  const tempDiv = document.createElement('div');
  tempDiv.style.position = 'absolute';
  tempDiv.style.left = '-9999px';
  tempDiv.style.top = '-9999px';
  tempDiv.style.width = '800px';
  tempDiv.style.padding = '0px 10px 5px 10px';
  tempDiv.style.backgroundColor = 'white';
  tempDiv.style.fontFamily = 'Arial, sans-serif';
  tempDiv.style.fontSize = '12px';

  tempDiv.innerHTML = `
    <div>
      <h1 style="font-size: 20px; margin-bottom: 10px; text-align: center;">
        Ausbildungsnachweis - Nr. ${reportData.reportNumber}
      </h1>
      <div style="margin-bottom: 8px;"><strong>Auszubildende/r:</strong> ${authStore.user}</div>
      <div style="margin-bottom: 8px;"><strong>Zeitraum:</strong> ${reportData.weekStart} - ${reportData.weekEnd}</div>
      <div style="margin-bottom: 8px;"><strong>Abteilung:</strong> ${reportData.department || 'Nicht angegeben'}</div>
      <div style="margin-bottom: 8px;"><strong>Status:</strong> ${getStatusText()}</div>
    </div>

    <div>
      <h2 style="font-size: 14px; margin-bottom: 8px; color: #333; border-bottom: 2px solid #333;">
        Betriebliche Tätigkeiten
      </h2>
      <div style="border: 1px solid #ccc; padding: 10px; min-height: 80px; background-color: #fafafa; line-height: 1.4;">
        ${processHtmlContent(reportData.weekText)}
      </div>
    </div>

    <div>
      <h2 style="font-size: 14px; margin-bottom: 8px; color: #333; border-bottom: 2px solid #333;">
        Unterweisungen
      </h2>
      <div style="border: 1px solid #ccc; padding: 10px; min-height: 80px; background-color: #fafafa; line-height: 1.4;">
        ${processHtmlContent(reportData.instructionText)}
      </div>
    </div>

    <div>
      <h2 style="font-size: 14px; margin-bottom: 8px; color: #333; border-bottom: 2px solid #333;">
        Berufsschule
      </h2>
      <div style="border: 1px solid #ccc; padding: 10px; min-height: 80px; background-color: #fafafa; line-height: 1.4;">
        ${processHtmlContent(reportData.schoolText)}
      </div>
    </div>

    ${reportData.extraText ? `
      <div>
        <h2 style="font-size: 14px; margin-bottom: 8px; color: #333; border-bottom: 2px solid #333;">
          Sonstiges
        </h2>
        <div style="border: 1px solid #ccc; padding: 10px; min-height: 80px; background-color: #fafafa; line-height: 1.4;">
          ${processHtmlContent(reportData.extraText)}
        </div>
      </div>
    ` : ''}
  `;

  document.body.appendChild(tempDiv);

  // Canvas erstellen
  const canvas = await html2canvas(tempDiv, {
    scale: 2,
    useCORS: true,
    allowTaint: true,
    backgroundColor: '#ffffff',
    logging: false,
  });

  // Temporäres Element entfernen
  document.body.removeChild(tempDiv);

  // PDF erstellen falls noch nicht vorhanden
  if (!pdf) {
    pdf = new jsPDF('p', 'mm', 'a4');
  }

  const imgData = canvas.toDataURL('image/jpeg');
  const pdfWidth = pdf.internal.pageSize.getWidth();
  const pdfHeight = pdf.internal.pageSize.getHeight();
  const canvasWidth = canvas.width;
  const canvasHeight = canvas.height;

  // Berechne die Skalierung (mm zu px Konvertierung)
  const mmToPx = 3.78; // 1mm = 3.78px bei 96 DPI
  const imgWidthMM = canvasWidth / mmToPx;
  const imgHeightMM = canvasHeight / mmToPx;

  // Skaliere das Bild, um in die PDF-Seite zu passen
  const margin = 10;
  const availableWidth = pdfWidth - 2 * margin;
  const availableHeight = pdfHeight - 2 * margin;

  let finalWidth = imgWidthMM;
  let finalHeight = imgHeightMM;

  // Skaliere falls nötig
  if (imgWidthMM > availableWidth) {
    const ratio = availableWidth / imgWidthMM;
    finalWidth = availableWidth;
    finalHeight = imgHeightMM * ratio;
  }

  // Zentriere das Bild horizontal
  const x = (pdfWidth - finalWidth) / 2;
  const y = margin;

  // Wenn das Bild in eine Seite passt, füge es direkt hinzu
  if (finalHeight <= availableHeight) {
    pdf.addImage(imgData, 'JPEG', x, y, finalWidth, finalHeight);
  } else {
    // Teile das Bild auf mehrere Seiten auf
    const pageHeight = availableHeight;
    const totalPages = Math.ceil(finalHeight / pageHeight);

    for (let i = 0; i < totalPages; i++) {
      if (i > 0) {
        pdf.addPage();
      }

      const sourceY = (i * pageHeight / finalHeight) * canvasHeight;
      const sourceHeight = Math.min(pageHeight / finalHeight * canvasHeight, canvasHeight - sourceY);
      const targetHeight = (sourceHeight / canvasHeight) * finalHeight;

      // Erstelle ein Canvas für diesen Seitenausschnitt
      const pageCanvas = document.createElement('canvas');
      pageCanvas.width = canvasWidth;
      pageCanvas.height = sourceHeight;
      const pageCtx = pageCanvas.getContext('2d');

      pageCtx.drawImage(canvas, 0, sourceY, canvasWidth, sourceHeight, 0, 0, canvasWidth, sourceHeight);

      const pageImgData = pageCanvas.toDataURL('image/jpeg');
      pdf.addImage(pageImgData, 'JPEG', x, y, finalWidth, targetHeight);
    }
  }

  return pdf;
}

// Einzelnen Bericht als PDF generieren
async function generatePDFWithFormatting() {
  try {
    const pdf = await generatePDFForReport(report.value);

    const fileName = `Ausbildungsnachweis_Woche_${report.value.reportNumber}.pdf`;
    pdf.save(fileName);

    $q.notify({
      type: 'positive',
      message: 'PDF mit Formatierung erfolgreich erstellt'
    });

  } catch (error) {
    console.error('Fehler beim Erstellen der PDF:', error);
    $q.notify({
      type: 'negative',
      message: 'Fehler beim Erstellen der PDF'
    });
  }
}

// Hilfsfunktion um einen einzelnen Bericht zu laden
async function loadReportData(weekStart) {
  try {
    const response = await api.get(`/api/report/${weekStart}`);
    return ReportDto.fromObject(response.data);
  } catch (error) {
    console.log(error)
    // Leeren Bericht zurückgeben falls nicht gefunden
    return null;
  }
}

// PDF für alle Berichte generieren
async function generateAllReportsPDF() {
  try {
    $q.loading.show({
      message: 'PDFs werden erstellt...'
    });

    let pdf = null;
    let reportCount = 0;

    for (const week of weeks.value) {
      const reportData = await loadReportData(week.weekStart);

      if (reportData) {
        if (reportCount > 0) {
          pdf.addPage();
        }

        pdf = await generatePDFForReport(reportData, pdf, reportCount === 0);
        reportCount++;
      }
    }

    if (pdf && reportCount > 0) {
      const fileName = `Alle_Ausbildungsnachweise_${authStore.user}.pdf`;
      pdf.save(fileName);

      $q.notify({
        type: 'positive',
        message: `PDF mit allen Berichten erstellt (${reportCount} Berichte)`
      });
    } else {
      $q.notify({
        type: 'warning',
        message: 'Keine Berichte zum Erstellen gefunden'
      });
    }

  } catch (error) {
    console.error('Fehler beim Erstellen der PDF:', error);
    $q.notify({
      type: 'negative',
      message: 'Fehler beim Erstellen der PDF'
    });
  } finally {
    $q.loading.hide();
  }
}

// Dialog für Auswahl mehrerer Berichte
function showMultiSelectDialog() {
  const options = weeks.value.map(week => ({
    label: `Woche ${week.reportNumber}: ${week.weekStart} - ${week.weekEnd}`,
    value: week.reportNumber,
    color: getStatusColor(week)
  }));

  $q.dialog({
    title: 'Berichte für PDF auswählen',
    message: 'Wählen Sie die Berichte aus, die in die PDF aufgenommen werden sollen:',
    options: {
      type: 'checkbox',
      model: [],
      items: options
    },
    cancel: {
      label: 'Abbrechen',
      color: 'grey',
      flat: true
    },
    ok: {
      label: 'OK',
      color: 'positive'
    },
    persistent: true
  }).onOk(selectedReports => {
    if (selectedReports.length > 0) {
      generateSelectedReportsPDF(selectedReports);
    }
  });
}

// PDF für ausgewählte Berichte generieren
async function generateSelectedReportsPDF(selectedReportNumbers) {
  try {
    $q.loading.show({
      message: `${selectedReportNumbers.length} PDFs werden erstellt...`
    });

    let pdf = null;
    let reportCount = 0;

    for (const reportNumber of selectedReportNumbers) {
      const week = weeks.value.find(w => w.reportNumber === reportNumber);
      if (week) {
        const reportData = await loadReportData(week.weekStart);

        if (reportData) {
          if (reportCount > 0) {
            pdf.addPage();
          }

          pdf = await generatePDFForReport(reportData, pdf, reportCount === 0);
          reportCount++;
        }
      }
    }

    if (pdf && reportCount > 0) {
      const fileName = `Ausgewählte_Berichte_${reportCount}_${authStore.user}.pdf`;
      pdf.save(fileName);

      $q.notify({
        type: 'positive',
        message: `PDF mit ${reportCount} Berichten erstellt`
      });
    } else {
      $q.notify({
        type: 'warning',
        message: 'Keine Berichte zum Erstellen gefunden'
      });
    }

  } catch (error) {
    console.error('Fehler beim Erstellen der PDF:', error);
    $q.notify({
      type: 'negative',
      message: 'Fehler beim Erstellen der PDF'
    });
  } finally {
    $q.loading.hide();
  }
}

// << LIFECYCLE HOOKS >>
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
  await fetchDateRange(true);
});
</script>

<style scoped>
.week-item {
  border-bottom: 1px solid rgba(0, 0, 0, 0.3);
  transition: background-color 0.3s;
}
.week-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}
.bg-grey {
  background-color: #c0c0c0 !important;
}
</style>
