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
      show-if-above
      bordered
    >
      <q-list>
        <q-item-label
          header
        >
          Navigation
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
import { onMounted, ref } from 'vue'
import EssentialLink from 'components/EssentialLink.vue'
import UserMenu from 'components/UserMenu.vue';
import { useAuthStore } from 'stores/auth.js';

const authStore = useAuthStore();
const linksList = [
  {
    title: 'Profil',
    caption: 'Profil anzeigen und bearbeiten',
    icon: 'person',
    link: '/profile'
  },
]

const leftDrawerOpen = ref(false)

function toggleLeftDrawer () {
  leftDrawerOpen.value = !leftDrawerOpen.value
}

onMounted(() => {
  if (authStore.isAdmin) {
    linksList.push({
      title: 'Benutzer',
      caption: 'Benutzer anzeigen und bearbeiten',
      icon: 'groups',
      link: '/user'
    },
    {
      title: 'Verwaltung',
      caption: 'Ausbilder und Auszubildende verwalten',
      icon: 'supervisor_account',
      link: '/manage-connections'
    })
  }
  if (authStore.isSupervisor || authStore.isAdmin) {
    linksList.push({
      title: 'Berichte',
      caption: 'Berichte einsehen',
      icon: 'article',
      link: '/report-watch'
    },
    {
     title: 'Dokumente',
     caption: 'Dokumente einsehen',
     icon: 'description',
     link: '/documents-watch'
    })
  } else {
    linksList.push({
      title: 'Berichte',
      caption: 'Berichte erstellen und bearbeiten',
      icon: 'article',
      link: '/report'
    },
    {
      title: 'Dokumente',
      caption: 'Dokumente hochladen und einsehen',
      icon: 'description',
      link: '/documents'
    })
  }
})
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
