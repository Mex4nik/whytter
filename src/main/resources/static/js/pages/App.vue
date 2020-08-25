<template>
    <v-app>
        <v-app-bar app color="primary" dark>
            <v-toolbar-title>Whytter</v-toolbar-title>
            <v-btn v-if="profile"
                   text
                   :disabled="$route.path === '/'"
                   @click="showMessages"
            >
                Message
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn v-if="profile"
                   text
                   :disabled="$route.path === '/profile'"
                   @click="showProfile"
            >
                {{ profile.name }}
            </v-btn>
            <v-btn v-if="profile" icon href="/logout">
                <v-icon>exit_to_app</v-icon>
            </v-btn>
        </v-app-bar>

        <v-main>
            <router-view></router-view>
        </v-main>
    </v-app>
</template>

<script>
    import { addHandler } from 'util/ws'
    import { mapState, mapMutations } from 'vuex'

    export default {
        computed: mapState(['profile']),
        methods: {
            ...mapMutations(['addMessageMutation', 'updateMessageMutation', 'removeMessageMutation']),
            showMessages() {
                this.$router.push('/')
            },
            showProfile() {
                this.$router.push('/profile')
            }
        },
        created() {
            addHandler(data => {
                if (data.objectType === 'MESSAGE') {
                    switch (data.eventType) {
                        case 'CREATE':
                            this.addMessageMutation(data.body)
                            break
                        case 'UPDATE':
                            this.updateMessageMutation(data.body)
                            break
                        case 'REMOVE':
                            this.removeMessageMutation(data.body)
                            break
                        default:
                            console.error(`Event type is unknown '${data.eventType}'`)
                    }

                } else {
                    console.error(`Object type is unknown '${data.objectType}'`)
                }

            })
        },
        beforeMount() {
            if (!this.profile) {
                this.$router.replace('/auth')
            }
        }
    }
</script>

<style scoped>
</style>