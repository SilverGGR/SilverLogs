<template>
  <div v-if="authStore.isAuthenticated">
    <q-card class="q-pa-md">
      <div class="text-h6">Token läuft ab in:</div>
      <div class="text-h5 q-mt-sm">
        {{ formattedTime }}
      </div>
      <q-btn @click="authStore.logout()" style="background: aqua">Logout</q-btn>
      <q-btn @click="authStore.verifyToken()" style="background: aqua">verify</q-btn>

    </q-card>
  </div>
  <div v-else>
    <q-card class="q-pa-md">
      <div class="text-h6">Nicht eingeloggt</div>
      <q-btn @click="toLogin()" style="background: aqua">Logout</q-btn>
      <q-btn @click="authStore.verifyToken()" style="background: aqua">verify</q-btn>
    </q-card>
  </div>
  <div>
    <!-- Admin-only Bereich -->
    <div v-if="authStore.isAdmin">
      <h2>Admin-Bereich</h2>
    </div>

    <!-- Für alle Benutzer -->
    <div>
      <h2>Mein Profil</h2>
      <q-btn @click="adminCheck">1</q-btn>
      <q-btn @click="adminCheck2">2</q-btn>
      <q-btn @click="hasAcces">Acc</q-btn>
    </div>
  </div>

</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from 'stores/auth'
import { useRouter } from 'vue-router'
import { api } from 'src/boot/axios';
import { Notify } from 'quasar';

const router = useRouter()
const authStore = useAuthStore()

function toLogin() {
  authStore.logout()
  router.push('/login');
}

function hasAcces() {
  console.log(authStore.hasPermission('ADMIN'));
}
async function adminCheck() {
  console.log(authStore.isAdmin)
  try {
    const response = await api.get('/api/admin/checkAdmin')

    if (response.data) {
      console.log(response.data)
      Notify.create({
        type: 'positive',
        message: 'Successful verify'
      });
      return true;
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.message || 'Verify failed'
    });
    return false;
  }
}

async function adminCheck2() {
  try {
    const response = await api.get('/api/checkAdmin')

    if (response.data) {
      console.log(response.data)
      Notify.create({
        type: 'positive',
        message: 'Successful verify'
      });
      return true;
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data?.message || 'Verify failed'
    });
    return false;
  }
}

// Neuer reaktiver Wert für die verbleibende Zeit
const z = ref(0)

const formatTime = (ms) => {
  if (!ms || ms <= 0) return '00:00'
  const min = Math.floor(ms / 60000)
  const sec = Math.floor((ms % 60000) / 1000)
  return `${min}:${sec.toString().padStart(2, '0')}`
}

const formattedTime = computed(() => formatTime(z.value))

let intervalId

function update() {
  // Berechne verbleibende Zeit direkt in der Komponente
  if (authStore.tokenExpiration) {
    z.value = Math.max(0, authStore.tokenExpiration - Date.now())
  } else {
    z.value = 0
  }
}

// Timer im Sekundentakt starten, um z zu aktualisieren
onMounted(() => {
  update()
  intervalId = setInterval(update, 1000)
})

onUnmounted(() => {
  clearInterval(intervalId)
})
</script>
