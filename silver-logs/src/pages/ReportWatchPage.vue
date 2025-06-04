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

      <!-- Loading Overlay -->
      <q-inner-loading :showing="loading">
        <q-spinner size="50px" color="primary" />
      </q-inner-loading>
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
const apprenticeOptions = ref([]);
const selectedApprentice = ref();

// << FUNCTIONS >>
async function loadApprentices() {
  try {
    loading.value = true
    const response = await api.get('/api/authUser/apprentice-for-supervisors')
    apprenticeOptions.value = response.data
    console.log(response.data)
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
// << LIFECYCLE HOOKS >>
onMounted(async () => {
  await loadApprentices()
})
</script>

<style scoped>

</style>
