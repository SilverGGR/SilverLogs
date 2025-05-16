<template>
  <div class="profile-image-container">
    <div class="profile-image-wrapper">
      <img
        v-if="imageUrl"
        :src="imageUrl"
        class="profile-image"
        alt="Profilbild"
      />
      <div v-else class="profile-placeholder">
        <q-icon name="person" size="80px" color="grey-7" />
      </div>

      <div class="upload-overlay" @click="triggerFileInput">
        <q-icon name="photo_camera" size="24px" color="white" />
      </div>
    </div>

    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      class="hidden-input"
      @change="onFileChange"
    />

    <q-dialog v-model="cropping">
      <q-card style="min-width: 350px">
        <q-card-section class="row items-center q-pb-none">
          <div class="text-h6">Bild zuschneiden</div>
          <q-space />
          <q-btn icon="close" flat round dense v-close-popup />
        </q-card-section>

        <q-card-section>
          <!-- Hier könnte eine Bildbearbeitung integriert werden -->
          <div class="text-center q-pa-md">
            <q-btn color="primary" label="Bild speichern" @click="uploadImage" />
          </div>
        </q-card-section>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { api } from 'src/boot/axios';
import { Notify } from 'quasar';

const imageUrl = ref(null);
const fileInput = ref(null);
const selectedFile = ref(null);
const cropping = ref(false);

onMounted(() => {
  loadUserImage();
});

const loadUserImage = async () => {
  try {
    // Abfrage des Profilbilds mit Cache-Busting Parameter
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
};

const triggerFileInput = () => {
  fileInput.value.click();
};

const onFileChange = (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // Dateityp prüfen
  if (!file.type.startsWith('image/')) {
    Notify.create({
      type: 'negative',
      message: 'Bitte wähle eine Bilddatei aus'
    });
    return;
  }

  // Dateigröße prüfen (max 10MB)
  if (file.size > 2 * 1024 * 1024) {
    Notify.create({
      type: 'negative',
      message: 'Das Bild darf nicht größer als 2MB sein'
    });
    return;
  }

  selectedFile.value = file;

  // Vorschaubild erzeugen
  imageUrl.value = URL.createObjectURL(file);

  // Hier könnte das Zuschneiden aktiviert werden
  // cropping.value = true;

  // Ohne Zuschneiden direkt hochladen
  uploadImage();
};

const uploadImage = async () => {
  if (!selectedFile.value) return;

  const formData = new FormData();
  formData.append('file', selectedFile.value);

  try {
    const response = await api.post('/api/authUser/upload-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    console.log(response);
    Notify.create({
      type: 'positive',
      message: 'Profilbild erfolgreich aktualisiert'
    });

    cropping.value = false;
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data || 'Fehler beim Hochladen des Bildes'
    });
  }
};
</script>

<style lang="scss" scoped>
.profile-image-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.profile-image-wrapper {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f0f0f0;
  border: 3px solid #fff;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0e0e0;
}

.upload-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.5);
  padding: 8px 0;
  display: flex;
  justify-content: center;
  cursor: pointer;
  transition: opacity 0.3s;
  opacity: 0;
}

.profile-image-wrapper:hover .upload-overlay {
  opacity: 1;
}

.hidden-input {
  display: none;
}
</style>
