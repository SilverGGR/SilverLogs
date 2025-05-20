<template>
  <q-layout view="IHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
          :class="{ 'rotate-icon': leftDrawerOpen}"
        />

        <q-toolbar-title>
          SilverLogs
        </q-toolbar-title>

        <UserMenu/>
      </q-toolbar>
    </q-header>

    <q-drawer
      v-model="leftDrawerOpen"
      bordered
    >
      <q-list>
        <q-item-label
          header
        >
          Essential Links
        </q-item-label>

        <EssentialLink
          v-for="link in linksList"
          :key="link.title"
          v-bind="link"
        />
        <q-btn
          flat
          color="primary"
          label="Home"
          to="/"
        />
        <q-btn
          flat
          color="primary"
          label="Profil"
          to="/profile"
        />

        <token-info/>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup>
import { ref } from 'vue'
import EssentialLink from 'components/EssentialLink.vue'
import TokenInfo from 'components/TokenInfo.vue';
import UserMenu from 'components/UserMenu.vue';

const linksList = [
  {
    title: 'Home',
    caption: 'Zur Startseite',
    icon: 'school',
    link: 'http://localhost:9000'
  },
  {
    title: 'Profil',
    caption: 'Profil anzeigen und bearbeiten',
    icon: 'school',
    link: 'http://localhost:9000/profile'
  }
]

const leftDrawerOpen = ref(false)

function toggleLeftDrawer () {
  leftDrawerOpen.value = !leftDrawerOpen.value
}
</script>

<style>
.rotate-icon .q-icon {
  transition: transform 0.3s ease-in-out;
  transform: rotate(-90deg);
}

.q-icon {
  transition: transform 0.3s ease-in-out;
}
</style>
