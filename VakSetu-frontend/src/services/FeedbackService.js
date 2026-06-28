import api from './api'

const FeedbackService = {
  submitFeedback(payload) {
    return api.post('/feedback', payload).then((response) => response.data)
  },
}

export default FeedbackService
