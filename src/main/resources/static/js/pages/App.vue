<template>
    <div>
        <div v-if="!profile">
            Авторизация через
            <a href="/login">Google</a>
        </div>
        <div v-else>
            <div>
                {{profile.name}}&nbsp;
                <a href="/logout">Выйти</a>
                <messages-list :messages="messages" />
            </div>
        </div>
    </div>
</template>

<script>
    import MessagesList from 'components/messages/MessageList.vue'

    export default {
        components: {
            MessagesList
        },
        data() {
            return {
                messages: frontendData.messages,
                profile: frontendData.profile
            }
        }
    }
</script>

<style scoped>

</style>