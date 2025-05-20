<template>
  <q-page padding>
    <div class="row justify-center q-mt-lg">
      <div class="col-12 col-md-8 col-lg-6">
        <q-card>
          <q-card-section class="text-center">
            <h2 class="text-h4 q-mb-lg">Mein Profil</h2>
            <ProfileImageContainer />
          </q-card-section>

          <q-card-section>
            <div class="row q-col-gutter-md">
              <div class="col-12 col-md-6">
                <q-input
                  v-model="profile.username"
                  label="Username"
                  readonly
                  outlined
                  dense
                >
                  <template v-slot:prepend>
                    <q-icon name="account_circle" />
                  </template>
                </q-input>
              </div>

              <div class="col-12 col-md-6">
                <q-input
                  v-model="profile.email"
                  label="E-Mail"
                  outlined
                  dense
                >
                  <template v-slot:prepend>
                    <q-icon name="email" />
                  </template>
                </q-input>
              </div>

              <div class="col-12 col-md-6">
                <q-input
                  v-model="profile.firstname"
                  label="Vorname"
                  outlined
                  dense
                >
                  <template v-slot:prepend>
                    <q-icon name="person" />
                  </template>
                </q-input>
              </div>

              <div class="col-12 col-md-6">
                <q-input
                  v-model="profile.lastname"
                  label="Nachname"
                  outlined
                  dense
                >
                  <template v-slot:prepend>
                    <q-icon name="person" />
                  </template>
                </q-input>
              </div>
            </div>

            <div class="row q-mt-lg">
              <div class="col-12 text-center">
                <q-btn
                  color="primary"
                  label="Profil aktualisieren"
                  @click="updateProfile"
                  :loading="loading"
                />
              </div>
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { api } from 'src/boot/axios';
import { Notify } from 'quasar';
import ProfileImageContainer from 'components/ProfileImageContainer.vue';

const profile = ref({
  username: '',
  email: '',
  firstname: '',
  lastname: ''
});
const loading = ref(false);

onMounted(async () => {
  await loadProfile();
});

const loadProfile = async () => {
  try {
    const response = await api.get('/api/authUser/current');
    profile.value = response.data;
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: 'Fehler beim Laden des Profils'
    });
    console.log(error);
  }
};

const updateProfile = async () => {
  loading.value = true;
  try {
    const response = await api.put('/api/authUser/update', {
      email: profile.value.email,
      firstname: profile.value.firstname,
      lastname: profile.value.lastname
    });
    console.log(response);

    Notify.create({
      type: 'positive',
      message: 'Profil erfolgreich aktualisiert'
    });
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data || 'Fehler beim Aktualisieren des Profils'
    });
  } finally {
    loading.value = false;
  }
};
</script>
