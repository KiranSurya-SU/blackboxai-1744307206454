const mongoose = require('mongoose');

const eventSchema = new mongoose.Schema({
  title: {
    type: String,
    required: [true, 'Event title is required']
  },
  description: {
    type: String,
    required: [true, 'Event description is required']
  },
  date: {
    type: Date,
    required: [true, 'Event date is required']
  },
  endDate: {
    type: Date
  },
  location: {
    type: String,
    required: [true, 'Event location is required']
  },
  type: {
    type: String,
    enum: ['online', 'offline', 'hybrid'],
    required: [true, 'Event type is required']
  },
  organizer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  capacity: {
    type: Number
  },
  registrationDeadline: {
    type: Date
  },
  status: {
    type: String,
    enum: ['upcoming', 'ongoing', 'completed', 'cancelled'],
    default: 'upcoming'
  },
  attendees: [{
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User'
    },
    registeredAt: {
      type: Date,
      default: Date.now
    },
    status: {
      type: String,
      enum: ['registered', 'attended', 'cancelled'],
      default: 'registered'
    }
  }],
  category: {
    type: String,
    enum: ['networking', 'workshop', 'seminar', 'reunion', 'career_fair', 'other'],
    required: true
  },
  image: {
    type: String
  },
  meetingLink: {
    type: String
  },
  agenda: [{
    time: String,
    description: String
  }],
  speakers: [{
    name: String,
    designation: String,
    organization: String,
    bio: String,
    image: String
  }]
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Indexes for better search performance
eventSchema.index({ title: 'text', description: 'text' });
eventSchema.index({ date: 1, status: 1 });

// Virtual field for number of attendees
eventSchema.virtual('attendeeCount').get(function() {
  return this.attendees.length;
});

// Pre-save middleware to update status based on date
eventSchema.pre('save', function(next) {
  const now = new Date();
  if (this.date > now) {
    this.status = 'upcoming';
  } else if (this.endDate && this.endDate < now) {
    this.status = 'completed';
  } else if (this.date <= now && (!this.endDate || this.endDate >= now)) {
    this.status = 'ongoing';
  }
  next();
});

// Method to check if event is full
eventSchema.methods.isFull = function() {
  return this.capacity && this.attendees.length >= this.capacity;
};

// Method to check if registration is open
eventSchema.methods.isRegistrationOpen = function() {
  const now = new Date();
  return (
    this.status === 'upcoming' &&
    (!this.registrationDeadline || this.registrationDeadline > now) &&
    !this.isFull()
  );
};

const Event = mongoose.model('Event', eventSchema);

module.exports = Event;
