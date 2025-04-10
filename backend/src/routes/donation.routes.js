const express = require('express');
const router = express.Router();
const donationController = require('../controllers/donation.controller');
const { protect, restrictTo } = require('../middleware/auth.middleware');

// All routes below this middleware are protected
router.use(protect);

// Routes accessible by all users
router.post('/', donationController.createDonation);
router.get('/user/mydonations', donationController.getMyDonations);
router.get('/stats', donationController.getDonationStats);

// Admin routes
router.get('/', restrictTo('admin'), donationController.getAllDonations);
router.put('/:id/status', restrictTo('admin'), donationController.updateDonationStatus);

module.exports = router;
