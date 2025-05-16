<template>
  <q-layout>
    <q-page-container>
      <div class="window-height window-width row justify-center items-center">
        <div class="column q-pa-lg">
          <div class="row">
            <q-card square class="shadow-24 register-card">
              <q-card-section class="bg-primary text-white">
                <h4 class="text-h5 text-white q-my-sm">Registrieren</h4>
              </q-card-section>

              <q-card-section style="padding: 30px">
                <q-form @submit.prevent="onSubmit">
                  <div class="row q-col-gutter-md">
                    <!-- LINKE SPALTE -->
                    <div class="col-12 col-md-6">
                      <!-- USERNAME -->
                      <q-input
                        square
                        v-model="username"
                        type="text"
                        label="Username"
                        class="q-mb-md"
                        :rules="[val => !!val || 'Username ist erforderlich']"
                      >
                        <template v-slot:prepend>
                          <q-icon name="account_circle" />
                        </template>
                      </q-input>

                      <!-- FIRSTNAME -->
                      <q-input
                        square
                        v-model="firstname"
                        type="text"
                        label="Vorname"
                        class="q-mb-md"
                        :rules="[val => !!val || 'Vorname ist erforderlich']"
                      >
                        <template v-slot:prepend>
                          <q-icon name="person" />
                        </template>
                      </q-input>

                      <!-- LASTNAME -->
                      <q-input
                        square
                        v-model="lastname"
                        type="text"
                        label="Nachname"
                        class="q-mb-md"
                        :rules="[val => !!val || 'Nachname ist erforderlich']"
                      >
                        <template v-slot:prepend>
                          <q-icon name="person" />
                        </template>
                      </q-input>
                    </div>

                    <!-- RECHTE SPALTE -->
                    <div class="col-12 col-md-6">
                      <!-- EMAIL -->
                      <q-input
                        square
                        v-model="email"
                        type="email"
                        label="E-Mail"
                        class="q-mb-md"
                        :rules="[
                          val => !!val || 'E-Mail ist erforderlich',
                          val => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val) || 'Gültige E-Mail eingeben'
                        ]"
                      >
                        <template v-slot:prepend>
                          <q-icon name="email" />
                        </template>
                      </q-input>

                      <!-- PASSWORD -->
                      <q-input
                        square
                        v-model="password"
                        :type="isPwd ? 'password' : 'text'"
                        label="Passwort"
                        class="q-mb-md"
                        :rules="[
                          val => !!val || 'Passwort ist erforderlich',
                          val => val.length >= 1 || 'Passwort muss mindestens 1 Zeichen lang sein'
                        ]"
                      >
                        <template v-slot:prepend>
                          <q-icon name="lock" />
                        </template>
                        <template v-slot:append>
                          <q-icon
                            :name="isPwd ? 'visibility_off' : 'visibility'"
                            class="cursor-pointer"
                            @click="isPwd = !isPwd"
                          />
                        </template>
                      </q-input>

                      <!-- CONFIRM PASSWORD -->
                      <q-input
                        square
                        v-model="confirmPassword"
                        :type="isPwdConfirm ? 'password' : 'text'"
                        label="Passwort bestätigen"
                        class="q-mb-md"
                        :rules="[
                          val => !!val || 'Passwortbestätigung ist erforderlich',
                          val => val === password || 'Passwörter stimmen nicht überein'
                        ]"
                      >
                        <template v-slot:prepend>
                          <q-icon name="enhanced_encryption" />
                        </template>
                        <template v-slot:append>
                          <q-icon
                            :name="isPwdConfirm ? 'visibility_off' : 'visibility'"
                            class="cursor-pointer"
                            @click="isPwdConfirm = !isPwdConfirm"
                          />
                        </template>
                      </q-input>
                    </div>
                  </div>

                  <div style="width: 50%; margin-left: 25%">
                    <q-btn
                      unelevated
                      size="lg"
                      color="primary"
                      class="full-width"
                      label="Registrieren"
                      type="submit"
                      :loading="loading"
                    />
                  </div>
                </q-form>
              </q-card-section>

              <q-card-section class="text-center q-pa-sm">
                <p class="text-grey-6">Bereits registriert?</p>
                <q-btn
                  flat
                  color="primary"
                  label="Login"
                  to="/login"
                />
              </q-card-section>
            </q-card>
          </div>
        </div>
      </div>
    </q-page-container>
  </q-layout>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from 'stores/auth';
import { api } from 'src/boot/axios';
import { Notify } from 'quasar';

const router = useRouter();
const authStore = useAuthStore();

const username = ref('');
const firstname = ref('');
const lastname = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const isPwd = ref(true);
const isPwdConfirm = ref(true);
const loading = ref(false);

const onSubmit = async () => {
  if (!username.value ||
    !firstname.value ||
    !lastname.value ||
    !email.value ||
    !password.value ||
    password.value !== confirmPassword.value) {
    return;
  }

  loading.value = true;
  try {
    const response = await api.post('/api/auth/register', {
      username: username.value,
      firstname: firstname.value,
      lastname: lastname.value,
      email: email.value,
      password: password.value
    });

    if (response.status === 200) {
      Notify.create({
        type: 'positive',
        message: 'Registrierung erfolgreich!'
      });

      // Optional: Automatisch einloggen nach Registrierung
      const success = await authStore.login(username.value, password.value);
      if (success) {
        await router.push('/');
      } else {
        await router.push('/login');
      }
    }
  } catch (error) {
    Notify.create({
      type: 'negative',
      message: error.response?.data || 'Registrierung fehlgeschlagen'
    });
  } finally {
    loading.value = false;
  }
};
</script>

<style lang="scss" scoped>
.register-card {
  width: 650px;
  max-width: 95vw;

  @media (max-width: 600px) {
    width: 100%;
  }
}

.q-card {
  @media (max-width: 400px) {
    width: 100%;
  }
}
</style>
