const mongoose = require('mongoose');

const jobSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, 'Job title is required']
  },
  company: {
    type: String,
    required: [true, 'Company name is required']
  },
  description: {
    type: String,
    required: [true, 'Job description is required']
  },
  requirements: [{
    type: String
  }],
  location: {
    type: String,
    required: [true, 'Job location is required']
  },
  type: {
    type: String,
    enum: ['full-time', 'part-time', 'internship', 'contract'],
    required: [true, 'Job type is required']
  },
  salary: {
    type: String
  },
  postedBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  status: {
    type: String,
    enum: ['active', 'closed'],
    default: 'active'
  },
  applicationDeadline: {
    type: Date
  },
  experience: {
    type: String
  },
  skills: [{
    type: String
  }],
  applicationLink: {
    type: String
  },
  contactEmail: {
    type: String
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Add index for better search performance
jobSchema.index({ title: 'text', description: 'text', company: 'text' });

// Virtual populate for applications (if implemented)
jobSchema.virtual('applications', {
  ref: 'Application',
  localField: '_id',
  foreignField: 'job'
});

// Pre-save middleware to ensure contactEmail is set
jobSchema.pre('save', async function(next) {
  if (!this.contactEmail && this.postedBy) {
    const User = mongoose.model('User');
    const user = await User.findById(this.postedBy);
    if (user) {
      this.contactEmail = user.email;
    }
  }
  next();
});

const Job = mongoose.model('Job', jobSchema);

module.exports = Job;
