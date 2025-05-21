<template>
  <div class="q-pa-md">
    <h2>Übersicht der Kalenderwochen</h2>

    <div class="row q-gutter-md q-mb-md">
      <q-input
        v-model="startDateInput"
        label="Startdatum"
        type="date"
        dense
        outlined
       />
      <q-input
        v-model="endDateInput"
        label="Enddatum"
        type="date"
        dense
        outlined
      />
    </div>

    <q-list bordered>
      <q-item-label header>WOCHEN</q-item-label>
      <q-item
        v-for="w in weeks"
        :key="w.weekNumber + '-' + w.start.toISOString()"
      >
        <q-item-section>
          <div class="text-h6">KW {{ w.weekNumber }}</div>
          <div class="text-caption">
            {{ formatDate(w.start) }} – {{ formatDate(w.end) }}
          </div>
        </q-item-section>
      </q-item>
    </q-list>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import WeekInterval from 'src/dtos/WeekInterval.js'
import { QInput, QList, QItem, QItemSection, QItemLabel } from 'quasar'

const startDateInput = ref('2024-01-01')
const endDateInput   = ref('2024-03-31')

const weeks = computed(() => {
  const start = new Date(startDateInput.value)
  const end   = new Date(endDateInput.value)
  const list  = []

  let current = getMonday(start)
  while (current <= end) {
    const wNum    = getISOWeek(current)
    const wStart  = new Date(current)
    const wEnd    = new Date(current)
    wEnd.setDate(wEnd.getDate() + 6)

    // Hier die Instanziierung
    list.push(new WeekInterval(wNum, wStart, wEnd))
    current.setDate(current.getDate() + 7)
  }

  return list
})

function getMonday(d) {
  const dt  = new Date(d)
  const day = dt.getDay() || 7
  dt.setDate(dt.getDate() - day + 1)
  return dt
}

function getISOWeek(d) {
  const dt = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()))
  dt.setUTCDate(dt.getUTCDate() + 4 - (dt.getUTCDay() || 7))
  const yearStart = new Date(Date.UTC(dt.getUTCFullYear(), 0, 1))
  return Math.ceil((((dt - yearStart) / 86400000) + 1) / 7)
}

function formatDate(d) {
  return d.toLocaleDateString('de-DE', {
    day: '2-digit', month: '2-digit', year: 'numeric'
  })
}
</script>

<style scoped lang="scss">
h2 {
  margin-bottom: 1rem;
}
</style>
