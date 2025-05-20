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
      <q-card class="crop-dialog-card">
        <q-card-section class="row items-center q-pb-none">
          <div class="text-h6">Bild zuschneiden</div>
          <q-space />
          <q-btn icon="close" flat round dense v-close-popup @click="cancelCropping" />
        </q-card-section>

        <q-card-section class="crop-container">
          <div class="canvas-wrapper" ref="canvasWrapper">
            <canvas ref="cropCanvas"></canvas>

            <!-- Quadratischer Auswahlbereich mit Handles -->
            <div v-if="cropperInitialized" class="crop-square" ref="cropSquare"
                 :style="cropSquareStyle"
                 @mousedown="startDragSquare">

              <!-- Handles für das Ziehen an den Ecken und Rändern -->
              <div class="resize-handle top-left" @mousedown.stop="startResize('topLeft')"></div>
              <div class="resize-handle top-right" @mousedown.stop="startResize('topRight')"></div>
              <div class="resize-handle bottom-left" @mousedown.stop="startResize('bottomLeft')"></div>
              <div class="resize-handle bottom-right" @mousedown.stop="startResize('bottomRight')"></div>

              <div class="resize-handle top" @mousedown.stop="startResize('top')"></div>
              <div class="resize-handle right" @mousedown.stop="startResize('right')"></div>
              <div class="resize-handle bottom" @mousedown.stop="startResize('bottom')"></div>
              <div class="resize-handle left" @mousedown.stop="startResize('left')"></div>
            </div>
          </div>

          <div class="crop-instructions q-mb-md">
            Verschieben oder ziehen Sie den Rahmen, um den Ausschnitt zu wählen. Das Bild wird als Quadrat zugeschnitten.
          </div>

          <div class="text-center q-pa-md">
            <q-btn color="negative" label="Abbrechen" class="q-mr-md" @click="cancelCropping" />
            <q-btn color="primary" label="Bild zuschneiden" @click="cropAndUpload" />
          </div>
        </q-card-section>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, onBeforeUnmount } from 'vue';
import { api } from 'src/boot/axios';
import { Notify } from 'quasar';

const imageUrl = ref(null);
const fileInput = ref(null);
const selectedFile = ref(null);
const cropping = ref(false);
const cropCanvas = ref(null);
const canvasWrapper = ref(null);
const originalImage = ref(null);
const cropSquare = ref(null);
const cropperInitialized = ref(false);

// Zustand für den Ausschnittbereich
const cropBox = ref({
  x: 0,
  y: 0,
  size: 100,
  dragStartX: 0,
  dragStartY: 0,
  isResizing: false,
  isDragging: false,
  resizeHandle: null
});

// Event-Listener-Referenzen für Cleanup
let mouseUpListener = null;
let mouseMoveListener = null;

// Berechneter Stil für den quadratischen Ausschnitt
const cropSquareStyle = computed(() => {
  return {
    left: `${cropBox.value.x}px`,
    top: `${cropBox.value.y}px`,
    width: `${cropBox.value.size}px`,
    height: `${cropBox.value.size}px`,
  };
});

onMounted(() => {
  loadUserImage();
});

onBeforeUnmount(() => {
  removeEventListeners();
});

const loadUserImage = async () => {
  try {
    // Abfrage des Profilbilds
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

  // Dateigröße prüfen (max 2MB)
  if (file.size > 2 * 1024 * 1024) {
    Notify.create({
      type: 'negative',
      message: 'Das Bild darf nicht größer als 2MB sein'
    });
    return;
  }

  selectedFile.value = file;

  // Zuschneiden aktivieren und Bild auf Canvas laden
  cropping.value = true;

  // Warten bis DOM aktualisiert ist und Canvas verfügbar ist
  nextTick(() => {
    loadImageToCanvas(file);
  });
};

const loadImageToCanvas = (file) => {
  const img = new Image();
  const reader = new FileReader();

  reader.onload = (e) => {
    img.onload = () => {
      originalImage.value = img;

      if (!cropCanvas.value || !canvasWrapper.value) {
        console.error('Canvas oder Wrapper nicht gefunden');
        return;
      }

      // Canvas initialisieren
      const canvas = cropCanvas.value;
      const wrapper = canvasWrapper.value;

      // Canvas-Größe berechnen
      // Machen Sie das Canvas so groß wie möglich, aber mit guter Darstellung
      const maxWidth = Math.min(window.innerWidth * 0.8, 800);
      const maxHeight = Math.min(window.innerHeight * 0.7, 800);

      let canvasWidth, canvasHeight;

      if (img.width > img.height) {
        canvasWidth = maxWidth;
        canvasHeight = (img.height / img.width) * canvasWidth;
      } else {
        canvasHeight = maxHeight;
        canvasWidth = (img.width / img.height) * canvasHeight;
      }

      // Canvas-Größe setzen
      canvas.width = canvasWidth;
      canvas.height = canvasHeight;

      // Wrapper-Größe anpassen
      wrapper.style.width = `${canvasWidth}px`;
      wrapper.style.height = `${canvasHeight}px`;

      // Bild auf Canvas zeichnen
      const ctx = canvas.getContext('2d');
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

      // Initialisiere das Ausschnitt-Quadrat
      initCropSquare(canvas);

      // Event-Listener für Mausbewegungen hinzufügen
      addEventListeners();
    };

    img.src = e.target.result;
  };

  reader.readAsDataURL(file);
};

const initCropSquare = (canvas) => {
  // Quadrat in der Mitte positionieren mit optimaler Größe
  const size = Math.min(canvas.width, canvas.height) * 0.8;

  cropBox.value = {
    x: (canvas.width - size) / 2,
    y: (canvas.height - size) / 2,
    size: size,
    dragStartX: 0,
    dragStartY: 0,
    isResizing: false,
    isDragging: false,
    resizeHandle: null
  };

  cropperInitialized.value = true;
};

const startDragSquare = (event) => {
  event.preventDefault();

  cropBox.value.isDragging = true;
  cropBox.value.dragStartX = event.clientX;
  cropBox.value.dragStartY = event.clientY;
};

const startResize = (handle) => {
  cropBox.value.isResizing = true;
  cropBox.value.resizeHandle = handle;
};

const moveSquare = (event) => {
  if (!cropBox.value.isDragging && !cropBox.value.isResizing) return;

  const canvas = cropCanvas.value;
  if (!canvas) return;

  if (cropBox.value.isDragging) {
    // Verschieben des Quadrats
    const deltaX = event.clientX - cropBox.value.dragStartX;
    const deltaY = event.clientY - cropBox.value.dragStartY;

    cropBox.value.x = Math.max(0, Math.min(canvas.width - cropBox.value.size, cropBox.value.x + deltaX));
    cropBox.value.y = Math.max(0, Math.min(canvas.height - cropBox.value.size, cropBox.value.y + deltaY));

    cropBox.value.dragStartX = event.clientX;
    cropBox.value.dragStartY = event.clientY;
  } else if (cropBox.value.isResizing) {
    // Größe des Quadrats ändern
    const rect = canvas.getBoundingClientRect();
    const mouseX = event.clientX - rect.left;
    const mouseY = event.clientY - rect.top;

    let newX = cropBox.value.x;
    let newY = cropBox.value.y;
    let newSize = cropBox.value.size;

    switch (cropBox.value.resizeHandle) {
      case 'topLeft':
        newSize = Math.min(
          cropBox.value.x + cropBox.value.size - Math.max(0, mouseX),
          cropBox.value.y + cropBox.value.size - Math.max(0, mouseY)
        );
        newX = cropBox.value.x + cropBox.value.size - newSize;
        newY = cropBox.value.y + cropBox.value.size - newSize;
        break;
      case 'topRight':
        newSize = Math.min(
          Math.min(canvas.width, mouseX) - cropBox.value.x,
          cropBox.value.y + cropBox.value.size - Math.max(0, mouseY)
        );
        newY = cropBox.value.y + cropBox.value.size - newSize;
        break;
      case 'bottomLeft':
        newSize = Math.min(
          cropBox.value.x + cropBox.value.size - Math.max(0, mouseX),
          Math.min(canvas.height, mouseY) - cropBox.value.y
        );
        newX = cropBox.value.x + cropBox.value.size - newSize;
        break;
      case 'bottomRight':
        newSize = Math.min(
          Math.min(canvas.width, mouseX) - cropBox.value.x,
          Math.min(canvas.height, mouseY) - cropBox.value.y
        );
        break;
      case 'top':
        newSize = cropBox.value.y + cropBox.value.size - Math.max(0, mouseY);
        newY = cropBox.value.y + cropBox.value.size - newSize;
        break;
      case 'right':
        newSize = Math.min(canvas.width, mouseX) - cropBox.value.x;
        break;
      case 'bottom':
        newSize = Math.min(canvas.height, mouseY) - cropBox.value.y;
        break;
      case 'left':
        newSize = cropBox.value.x + cropBox.value.size - Math.max(0, mouseX);
        newX = cropBox.value.x + cropBox.value.size - newSize;
        break;
    }

    // Minimumgröße und Grenzen einhalten
    const minSize = 50;
    newSize = Math.max(minSize, newSize);

    // Grenzen prüfen
    if (newX < 0) {
      newSize = cropBox.value.x + cropBox.value.size;
      newX = 0;
    }
    if (newY < 0) {
      newSize = cropBox.value.y + cropBox.value.size;
      newY = 0;
    }
    if (newX + newSize > canvas.width) {
      newSize = canvas.width - newX;
    }
    if (newY + newSize > canvas.height) {
      newSize = canvas.height - newY;
    }

    // Werte aktualisieren
    cropBox.value.x = newX;
    cropBox.value.y = newY;
    cropBox.value.size = newSize;
  }
};

const endMove = () => {
  cropBox.value.isDragging = false;
  cropBox.value.isResizing = false;
};

const addEventListeners = () => {
  removeEventListeners(); // Erst alte entfernen, falls vorhanden

  mouseUpListener = () => endMove();
  mouseMoveListener = (e) => moveSquare(e);

  window.addEventListener('mouseup', mouseUpListener);
  window.addEventListener('mousemove', mouseMoveListener);
};

const removeEventListeners = () => {
  if (mouseUpListener) {
    window.removeEventListener('mouseup', mouseUpListener);
  }
  if (mouseMoveListener) {
    window.removeEventListener('mousemove', mouseMoveListener);
  }
};

const cropAndUpload = () => {
  if (!originalImage.value || !cropBox.value || !cropCanvas.value) return;

  // Originalkoordinaten aus der Canvas-Darstellung berechnen
  const canvas = cropCanvas.value;
  const img = originalImage.value;

  const scaleX = img.width / canvas.width;
  const scaleY = img.height / canvas.height;

  const x = cropBox.value.x * scaleX;
  const y = cropBox.value.y * scaleY;
  const size = cropBox.value.size * scaleX; // Wir verwenden scaleX, da wir ein Quadrat haben

  // Temporäre Canvas für den zugeschnittenen Bereich erstellen
  const tempCanvas = document.createElement('canvas');
  // Wir setzen hier eine feste Größe für das Profilbild
  const outputSize = 400; // Größe des zugeschnittenen Quadrats
  tempCanvas.width = outputSize;
  tempCanvas.height = outputSize;

  const ctx = tempCanvas.getContext('2d');
  ctx.drawImage(img, x, y, size, size * (scaleY/scaleX), 0, 0, outputSize, outputSize);

  // In Blob konvertieren und hochladen
  tempCanvas.toBlob((blob) => {
    // Dateiname und Typ vom Original übernehmen
    const croppedFile = new File([blob], selectedFile.value.name, {
      type: selectedFile.value.type
    });

    uploadProcessedImage(croppedFile);
  }, selectedFile.value.type, 0.95); // 0.95 = hohe Qualität
};

const cancelCropping = () => {
  cropping.value = false;
  cropperInitialized.value = false;
  removeEventListeners();
};

const uploadProcessedImage = async (file) => {
  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await api.post('/api/authUser/upload-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    console.log(response);
    // Erfolg
    Notify.create({
      type: 'positive',
      message: 'Profilbild erfolgreich aktualisiert'
    });

    // Dialog schließen und Seite neu laden
    cropping.value = false;
    cropperInitialized.value = false;
    removeEventListeners();
    window.location.reload();
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

.crop-dialog-card {
  min-width: 80vw;
  max-width: 900px;
  max-height: 90vh;
}

.crop-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  overflow-y: auto;
}

.canvas-wrapper {
  position: relative;
  margin-bottom: 20px;
  border: 1px solid #ddd;
  overflow: hidden;
}

.crop-square {
  position: absolute;
  border: 2px dashed #fff;
  box-shadow: 0 0 0 2000px rgba(0, 0, 0, 0.5);
  cursor: move;
  touch-action: none;
}

.crop-instructions {
  color: #555;
  text-align: center;
  max-width: 500px;
  margin: 1rem auto;
}

canvas {
  display: block;
  max-width: 100%;
}

/* Resize-Handles für die Ecken und Seiten */
.resize-handle {
  position: absolute;
  background-color: #fff;
  border: 1px solid #007bff;
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.top-left {
  top: -6px;
  left: -6px;
  cursor: nw-resize;
}

.top-right {
  top: -6px;
  right: -6px;
  cursor: ne-resize;
}

.bottom-left {
  bottom: -6px;
  left: -6px;
  cursor: sw-resize;
}

.bottom-right {
  bottom: -6px;
  right: -6px;
  cursor: se-resize;
}

.top {
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  cursor: n-resize;
}

.right {
  right: -6px;
  top: 50%;
  transform: translateY(-50%);
  cursor: e-resize;
}

.bottom {
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  cursor: s-resize;
}

.left {
  left: -6px;
  top: 50%;
  transform: translateY(-50%);
  cursor: w-resize;
}
</style>
