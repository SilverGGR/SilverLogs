<template>
  <q-layout>
    <q-page-container>
      <div class="window-height window-width row justify-center items-center">
        <div class="column q-pa-lg">
          <div class="row">
            <q-card square class="shadow-24" style="width: 300px">
              <q-card-section class="bg-primary text-white">
                <h4 class="text-h5 text-white q-my-sm">Login</h4>
              </q-card-section>

              <q-card-section>
                <q-form @submit="onSubmit" class="q-gutter-md">
                  <q-input
                    square
                    v-model="username"
                    type="text"
                    label="Username"
                    :rules="[val => !!val || 'Username ist erforderlich']"
                  >
                    <template v-slot:prepend>
                      <q-icon name="person" />
                    </template>
                  </q-input>

                  <q-input
                    square
                    v-model="password"
                    :type="isPwd ? 'password' : 'text'"
                    label="Password"
                    :rules="[val => !!val || 'Passwort ist erforderlich']"
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
                </q-form>
              </q-card-section>

              <q-card-actions class="q-px-lg">
                <q-btn
                  unelevated
                  size="lg"
                  color="primary"
                  class="full-width"
                  label="Login"
                  @click="onSubmit"
                  :loading="loading"
                />
              </q-card-actions>

              <q-card-section class="text-center q-pa-sm">
                <p class="text-grey-6">Noch kein Account?</p>
                <q-btn
                  flat
                  color="primary"
                  label="Registrieren"
                  to="/register"
                  class="q-ml-sm"
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

const router = useRouter();
const authStore = useAuthStore();

const username = ref('');
const password = ref('');
const isPwd = ref(true);
const loading = ref(false);

const onSubmit = async () => {
  if (!username.value || !password.value) return;

  loading.value = true;
  try {
    const success = await authStore.login(username.value, password.value);
    if (success) {
      await router.push('/');
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style lang="scss" scoped>
.q-card {
  width: 360px;

  @media (max-width: 400px) {
    width: 100%;
  }
}
</style>
