import api from './api'

const TopicService = {
  getTopics() {
    return api.get('/topics').then((response) => response.data)
  },
}

export default TopicService
