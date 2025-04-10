const mongoose = require('mongoose');

const donationSchema = new mongoose.Schema({
  donor: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  amount: {
    type: Number,
    required: [true, 'Donation amount is required'],
    min: [1, 'Donation amount must be at least 1']
  },
  currency: {
    type: String,
    default: 'USD',
    enum: ['USD', 'EUR', 'GBP', 'INR']
  },
  purpose: {
    type: String,
    required: [true, 'Donation purpose is required'],
    enum: [
      'student_scholarship',
      'infrastructure',
      'research',
      'sports',
      'library',
      'general',
      'other'
    ]
  },
  paymentMethod: {
    type: String,
    required: true,
    enum: ['credit_card', 'debit_card', 'bank_transfer', 'upi', 'paypal']
  },
  status: {
    type: String,
    enum: ['pending', 'completed', 'failed', 'refunded'],
    default: 'pending'
  },
  transactionId: {
    type: String,
    unique: true
  },
  receiptNumber: {
    type: String,
    unique: true
  },
  anonymous: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    maxlength: 500
  },
  taxDeductible: {
    type: Boolean,
    default: true
  },
  recurring: {
    type: Boolean,
    default: false
  },
  frequency: {
    type: String,
    enum: ['monthly', 'quarterly', 'annually', null],
    default: null
  },
  nextPaymentDate: {
    type: Date
  },
  paymentDetails: {
    last4: String,
    cardType: String,
    expiryMonth: Number,
    expiryYear: Number
  },
  billingAddress: {
    street: String,
    city: String,
    state: String,
    country: String,
    zipCode: String
  },
  taxReceipt: {
    issued: {
      type: Boolean,
      default: false
    },
    number: String,
    issuedDate: Date,
    documentUrl: String
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Indexes for better query performance
donationSchema.index({ donor: 1, createdAt: -1 });
donationSchema.index({ status: 1 });
donationSchema.index({ transactionId: 1 }, { unique: true });
donationSchema.index({ receiptNumber: 1 }, { unique: true });

// Generate receipt number
donationSchema.pre('save', async function(next) {
  if (this.isNew) {
    const date = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    
    // Get count of donations in current month
    const count = await mongoose.model('Donation').countDocuments({
      createdAt: {
        $gte: new Date(date.getFullYear(), date.getMonth(), 1),
        $lt: new Date(date.getFullYear(), date.getMonth() + 1, 1)
      }
    });
    
    // Generate receipt number: DON-YYYYMM-XXXX
    this.receiptNumber = `DON-${year}${month}-${String(count + 1).padStart(4, '0')}`;
  }
  next();
});

// Virtual for formatted amount
donationSchema.virtual('formattedAmount').get(function() {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: this.currency
  }).format(this.amount);
});

// Method to generate tax receipt
donationSchema.methods.generateTaxReceipt = async function() {
  if (this.status === 'completed' && this.taxDeductible && !this.taxReceipt.issued) {
    const date = new Date();
    this.taxReceipt = {
      issued: true,
      number: `TR-${this.receiptNumber}`,
      issuedDate: date,
      // URL generation logic would go here
      documentUrl: `/receipts/tax/${this.receiptNumber}.pdf`
    };
    await this.save();
  }
};

// Static method to get donation statistics
donationSchema.statics.getDonationStats = async function() {
  return this.aggregate([
    {
      $match: { status: 'completed' }
    },
    {
      $group: {
        _id: '$purpose',
        totalAmount: { $sum: '$amount' },
        count: { $sum: 1 }
      }
    }
  ]);
};

const Donation = mongoose.model('Donation', donationSchema);

module.exports = Donation;
