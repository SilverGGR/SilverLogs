
<template>
  <div class="profile-avatar-container" @click="toggleMenu">
    <div
      class="profile-avatar-wrapper clickable"
    >
      <img
        v-if="imageUrl"
        :src="imageUrl"
        class="profile-avatar"
        alt="Profilbild"
      />
      <div v-else class="avatar-placeholder">
        <q-icon name="person" :size="'20px'" color="grey-7" />
      </div>
    </div>
    <q-menu :v-model="showing" anchor="bottom right" self="top right">
      <q-list style="min-width: 100px">
        <q-item clickable v-close-popup>
          <q-item-section @click="router.push('/')">Home</q-item-section>
        </q-item>
        <q-item clickable v-close-popup>
          <q-item-section @click="router.push('/profile')">Benutzereinstellungen</q-item-section>
        </q-item>

        <q-separator />

          <q-item class="q-pa-md" style="display: flex; flex-direction: column">
            <div>Token läuft ab in:</div>
            <div class="text-h6 q-mt-sm">
              {{ formattedTime }}
            </div>
          </q-item>

        <q-item clickable v-close-popup>
          <q-item-section @click="logout">Abmelden</q-item-section>
        </q-item>
      </q-list>
    </q-menu>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { api } from 'src/boot/axios';
import { useRouter } from 'vue-router';
import { useQuasar } from 'quasar';
import { useAuthStore } from 'stores/auth.js';

// << HELPER >>
const router = useRouter();
const $q = useQuasar();
const authStore = useAuthStore();
const imageUrl = ref(null);
const showing = ref(false);

// << LOCAL VARIABLES >>
const z = ref(0)
const formatTime = (ms) => {
  if (!ms || ms <= 0) return '00:00'
  const min = Math.floor(ms / 60000)
  const sec = Math.floor((ms % 60000) / 1000)
  return `${min}:${sec.toString().padStart(2, '0')}`
}
const formattedTime = computed(() => formatTime(z.value))
let intervalId

// << FUNCTIONS >>
async function loadUserImage() {
  try {
    const response = await api.get('/api/authUser/image', {
      responseType: 'blob',
    });

    imageUrl.value = URL.createObjectURL(response.data);
  } catch (error) {
    if (error.response && error.response.status !== 404) {
      console.error('Fehler beim Laden des Profilbilds:', error);
    }
    // 404 bedeutet, dass kein Bild vorhanden ist - kein Fehler
  }
}

function toggleMenu() {
  showing.value = !showing.value;
}

function logout() {
  authStore.logout();
  router.push('/login');
  $q.notify({
    type: 'positive',
    message: 'You have been logged out',
  });
}

function update() {
  // Berechne verbleibende Zeit direkt in der Komponente
  if (authStore.tokenExpiration) {
    z.value = Math.max(0, authStore.tokenExpiration - Date.now())
  } else {
    z.value = 0
  }
}

  // << LIFECYCLE & HOOKS
onMounted(() => {
  loadUserImage();
  update()
  // Timer im Sekundentakt starten, um z zu aktualisieren
  intervalId = setInterval(update, 1000)
});

onUnmounted(() => {
  clearInterval(intervalId)
});

</script>

<style lang="scss" scoped>
.profile-avatar-container {
  display: inline-flex;
  align-items: center;
}

.profile-avatar-wrapper {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f0f0f0;
  border: 2px solid #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;

  &.clickable {
    cursor: pointer;

    &:hover {
      transform: scale(1.05);
    }
  }
}

.profile-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0e0e0;
}
</style>
