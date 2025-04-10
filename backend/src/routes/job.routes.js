const express = require('express');
const router = express.Router();
const jobController = require('../controllers/job.controller');
const { protect, restrictTo } = require('../middleware/auth.middleware');

// All routes below this middleware are protected
router.use(protect);

// Routes accessible by both students and alumni
router.get('/', jobController.getJobs);
router.get('/:id', jobController.getJobById);

// Routes restricted to alumni only
router.post('/', restrictTo('alumni'), jobController.createJob);
router.put('/:id', restrictTo('alumni'), jobController.updateJob);
router.delete('/:id', restrictTo('alumni'), jobController.deleteJob);
router.patch('/:id/close', restrictTo('alumni'), jobController.closeJob);

// Get jobs posted by current user (alumni only)
router.get('/user/myjobs', restrictTo('alumni'), jobController.getMyJobs);

module.exports = router;
