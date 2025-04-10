const mongoose = require('mongoose');

// Message Schema
const messageSchema = new mongoose.Schema({
  sender: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  content: {
    type: String,
    required: true
  },
  attachments: [{
    type: String,
    url: String,
    name: String
  }],
  readBy: [{
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User'
    },
    readAt: {
      type: Date,
      default: Date.now
    }
  }]
}, {
  timestamps: true
});

// Chat Schema
const chatSchema = new mongoose.Schema({
  type: {
    type: String,
    enum: ['individual', 'group'],
    required: true
  },
  name: {
    type: String,
    required: function() {
      return this.type === 'group';
    }
  },
  participants: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  }],
  messages: [messageSchema],
  lastMessage: {
    type: messageSchema,
    default: null
  },
  admin: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: function() {
      return this.type === 'group';
    }
  },
  groupImage: {
    type: String
  },
  description: {
    type: String
  },
  isActive: {
    type: Boolean,
    default: true
  },
  metadata: {
    participantCount: {
      type: Number,
      default: function() {
        return this.participants.length;
      }
    },
    messageCount: {
      type: Number,
      default: 0
    },
    lastActivity: {
      type: Date,
      default: Date.now
    }
  }
}, {
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
});

// Indexes for better query performance
chatSchema.index({ participants: 1 });
chatSchema.index({ 'messages.sender': 1 });
chatSchema.index({ 'messages.createdAt': -1 });

// Update metadata when new message is added
chatSchema.pre('save', function(next) {
  if (this.isModified('messages')) {
    this.metadata.messageCount = this.messages.length;
    this.metadata.lastActivity = Date.now();
    if (this.messages.length > 0) {
      this.lastMessage = this.messages[this.messages.length - 1];
    }
  }
  next();
});

// Virtual for unread messages count per user
chatSchema.virtual('unreadCount').get(function() {
  return function(userId) {
    if (!this.messages.length) return 0;
    
    const lastReadMessage = this.messages.reduce((latest, message) => {
      const readRecord = message.readBy.find(record => 
        record.user.toString() === userId.toString()
      );
      if (readRecord && (!latest || readRecord.readAt > latest)) {
        return readRecord.readAt;
      }
      return latest;
    }, null);

    if (!lastReadMessage) return this.messages.length;

    return this.messages.filter(message => 
      message.createdAt > lastReadMessage
    ).length;
  };
});

// Method to add message
chatSchema.methods.addMessage = async function(senderId, content, attachments = []) {
  this.messages.push({
    sender: senderId,
    content,
    attachments,
    readBy: [{ user: senderId }]
  });
  await this.save();
  return this.messages[this.messages.length - 1];
};

// Method to mark messages as read
chatSchema.methods.markAsRead = async function(userId) {
  const now = new Date();
  this.messages.forEach(message => {
    if (!message.readBy.some(record => record.user.toString() === userId.toString())) {
      message.readBy.push({ user: userId, readAt: now });
    }
  });
  await this.save();
};

// Static method to get or create individual chat
chatSchema.statics.getOrCreateIndividualChat = async function(user1Id, user2Id) {
  let chat = await this.findOne({
    type: 'individual',
    participants: { $all: [user1Id, user2Id], $size: 2 }
  });

  if (!chat) {
    chat = await this.create({
      type: 'individual',
      participants: [user1Id, user2Id]
    });
  }

  return chat;
};

const Chat = mongoose.model('Chat', chatSchema);

module.exports = Chat;
