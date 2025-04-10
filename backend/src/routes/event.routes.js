const express = require('express');
const router = express.Router();
const eventController = require('../controllers/event.controller');
const { protect, restrictTo } = require('../middleware/auth.middleware');

// All routes below this middleware are protected
router.use(protect);

// Routes accessible by both students and alumni
router.get('/', eventController.getEvents);
router.get('/:id', eventController.getEventById);
router.post('/:id/register', eventController.registerForEvent);
router.post('/:id/cancel', eventController.cancelRegistration);
router.get('/user/registered', eventController.getMyRegisteredEvents);

// Routes for creating and managing events (restricted to alumni)
router.post('/', restrictTo('alumni'), eventController.createEvent);
router.put('/:id', restrictTo('alumni'), eventController.updateEvent);
router.delete('/:id', restrictTo('alumni'), eventController.deleteEvent);
router.get('/user/organized', restrictTo('alumni'), eventController.getMyEvents);

module.exports = router;
