const express = require('express');
const router = express.Router();
const chatController = require('../controllers/chat.controller');
const { protect } = require('../middleware/auth.middleware');

// Protect all routes
router.use(protect);

// Chat routes
router.get('/', chatController.getMyChats);
router.get('/:id', chatController.getChatById);
router.post('/group', chatController.createGroupChat);
router.get('/individual/:userId', chatController.getOrCreateIndividualChat);
router.post('/:id/message', chatController.sendMessage);
router.put('/:id', chatController.updateGroupChat);
router.post('/:id/participants', chatController.addParticipant);
router.delete('/:id/participants/:userId', chatController.removeParticipant);
router.delete('/:id/leave', chatController.leaveChat);

module.exports = router;
