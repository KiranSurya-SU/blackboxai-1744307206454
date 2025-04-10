const Event = require('../models/event.model');

// Create a new event
exports.createEvent = async (req, res) => {
  try {
    const eventData = {
      ...req.body,
      organizer: req.user._id
    };

    const event = await Event.create(eventData);
    await event.populate('organizer', 'name email');

    res.status(201).json({
      message: 'Event created successfully',
      event
    });
  } catch (error) {
    console.error('Create event error:', error);
    res.status(500).json({ message: 'Error creating event' });
  }
};

// Get all events with filters and pagination
exports.getEvents = async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    // Build query based on filters
    const query = {};
    
    if (req.query.type) query.type = req.query.type;
    if (req.query.category) query.category = req.query.category;
    if (req.query.status) query.status = req.query.status;
    if (req.query.search) {
      query.$text = { $search: req.query.search };
    }

    // Date filters
    if (req.query.fromDate) {
      query.date = { $gte: new Date(req.query.fromDate) };
    }
    if (req.query.toDate) {
      query.date = { ...query.date, $lte: new Date(req.query.toDate) };
    }

    // Execute query with pagination
    const events = await Event.find(query)
      .populate('organizer', 'name email')
      .populate('attendees.user', 'name email')
      .sort({ date: 1 })
      .skip(skip)
      .limit(limit);

    // Get total count for pagination
    const total = await Event.countDocuments(query);

    res.json({
      events,
      pagination: {
        current: page,
        total: Math.ceil(total / limit),
        totalRecords: total
      }
    });
  } catch (error) {
    console.error('Get events error:', error);
    res.status(500).json({ message: 'Error fetching events' });
  }
};

// Get single event by ID
exports.getEventById = async (req, res) => {
  try {
    const event = await Event.findById(req.params.id)
      .populate('organizer', 'name email')
      .populate('attendees.user', 'name email');

    if (!event) {
      return res.status(404).json({ message: 'Event not found' });
    }

    res.json({ event });
  } catch (error) {
    console.error('Get event error:', error);
    res.status(500).json({ message: 'Error fetching event details' });
  }
};

// Update event
exports.updateEvent = async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);

    if (!event) {
      return res.status(404).json({ message: 'Event not found' });
    }

    // Check if user is the organizer
    if (event.organizer.toString() !== req.user._id.toString()) {
      return res.status(403).json({ message: 'Not authorized to update this event' });
    }

    const updatedEvent = await Event.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true, runValidators: true }
    ).populate('organizer', 'name email');

    res.json({
      message: 'Event updated successfully',
      event: updatedEvent
    });
  } catch (error) {
    console.error('Update event error:', error);
    res.status(500).json({ message: 'Error updating event' });
  }
};

// Delete event
exports.deleteEvent = async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);

    if (!event) {
      return res.status(404).json({ message: 'Event not found' });
    }

    // Check if user is the organizer
    if (event.organizer.toString() !== req.user._id.toString()) {
      return res.status(403).json({ message: 'Not authorized to delete this event' });
    }

    await event.remove();

    res.json({
      message: 'Event deleted successfully'
    });
  } catch (error) {
    console.error('Delete event error:', error);
    res.status(500).json({ message: 'Error deleting event' });
  }
};

// Register for an event
exports.registerForEvent = async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);

    if (!event) {
      return res.status(404).json({ message: 'Event not found' });
    }

    if (!event.isRegistrationOpen()) {
      return res.status(400).json({ message: 'Registration is closed for this event' });
    }

    // Check if user is already registered
    const isRegistered = event.attendees.some(
      attendee => attendee.user.toString() === req.user._id.toString()
    );

    if (isRegistered) {
      return res.status(400).json({ message: 'Already registered for this event' });
    }

    event.attendees.push({
      user: req.user._id,
      status: 'registered'
    });

    await event.save();
    await event.populate('attendees.user', 'name email');

    res.json({
      message: 'Successfully registered for the event',
      event
    });
  } catch (error) {
    console.error('Event registration error:', error);
    res.status(500).json({ message: 'Error registering for event' });
  }
};

// Cancel event registration
exports.cancelRegistration = async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);

    if (!event) {
      return res.status(404).json({ message: 'Event not found' });
    }

    const attendeeIndex = event.attendees.findIndex(
      attendee => attendee.user.toString() === req.user._id.toString()
    );

    if (attendeeIndex === -1) {
      return res.status(400).json({ message: 'Not registered for this event' });
    }

    event.attendees[attendeeIndex].status = 'cancelled';
    await event.save();

    res.json({
      message: 'Registration cancelled successfully',
      event
    });
  } catch (error) {
    console.error('Cancel registration error:', error);
    res.status(500).json({ message: 'Error cancelling registration' });
  }
};

// Get events organized by current user
exports.getMyEvents = async (req, res) => {
  try {
    const events = await Event.find({ organizer: req.user._id })
      .populate('attendees.user', 'name email')
      .sort({ date: -1 });

    res.json({ events });
  } catch (error) {
    console.error('Get my events error:', error);
    res.status(500).json({ message: 'Error fetching your events' });
  }
};

// Get events registered by current user
exports.getMyRegisteredEvents = async (req, res) => {
  try {
    const events = await Event.find({
      'attendees.user': req.user._id,
      'attendees.status': 'registered'
    })
      .populate('organizer', 'name email')
      .sort({ date: 1 });

    res.json({ events });
  } catch (error) {
    console.error('Get registered events error:', error);
    res.status(500).json({ message: 'Error fetching your registered events' });
  }
};
