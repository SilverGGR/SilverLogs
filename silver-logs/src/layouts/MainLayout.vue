<template>
  <q-layout view="hHh Lpr lFf">
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
import UserMenu from 'components/UserMenu.vue';

const linksList = [
  {
    title: 'Home',
    caption: 'Zur Startseite',
    icon: 'home',
    link: '/'
  },
  {
    title: 'Profil',
    caption: 'Profil anzeigen und bearbeiten',
    icon: 'person',
    link: '/profile'
  },
  {
    title: 'Berichte',
    caption: 'Berichte erstellen und bearbeiten',
    icon: 'description',
    link: '/report'
  },
  {
    title: 'Manager',
    caption: 'Ausbilder und Auszubildende verwalten',
    icon: 'supervisor_account',
    link: '/manage-users'
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
