const Donation = require('../models/donation.model');

// Create a new donation
exports.createDonation = async (req, res) => {
  try {
    const donationData = {
      ...req.body,
      donor: req.user._id
    };

    const donation = await Donation.create(donationData);

    res.status(201).json({
      message: 'Donation made successfully',
      donation
    });
  } catch (error) {
    console.error('Create donation error:', error);
    res.status(500).json({ message: 'Error creating donation' });
  }
};

// Get all donations made by the current user
exports.getMyDonations = async (req, res) => {
  try {
    const donations = await Donation.find({ donor: req.user._id })
      .sort({ createdAt: -1 });

    res.json({ donations });
  } catch (error) {
    console.error('Get my donations error:', error);
    res.status(500).json({ message: 'Error fetching your donations' });
  }
};

// Get donation statistics
exports.getDonationStats = async (req, res) => {
  try {
    const stats = await Donation.getDonationStats();
    res.json({ stats });
  } catch (error) {
    console.error('Get donation stats error:', error);
    res.status(500).json({ message: 'Error fetching donation statistics' });
  }
};

// Get all donations (admin only)
exports.getAllDonations = async (req, res) => {
  try {
    const donations = await Donation.find()
      .populate('donor', 'name email')
      .sort({ createdAt: -1 });

    res.json({ donations });
  } catch (error) {
    console.error('Get all donations error:', error);
    res.status(500).json({ message: 'Error fetching donations' });
  }
};

// Update donation status (admin only)
exports.updateDonationStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    const donation = await Donation.findByIdAndUpdate(id, { status }, { new: true });

    if (!donation) {
      return res.status(404).json({ message: 'Donation not found' });
    }

    res.json({
      message: 'Donation status updated successfully',
      donation
    });
  } catch (error) {
    console.error('Update donation status error:', error);
    res.status(500).json({ message: 'Error updating donation status' });
  }
};
