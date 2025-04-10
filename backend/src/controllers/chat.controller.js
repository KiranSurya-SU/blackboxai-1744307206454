const Chat = require('../models/chat.model');
const User = require('../models/user.model');

// Get all chats for current user
exports.getMyChats = async (req, res) => {
  try {
    const chats = await Chat.find({
      participants: req.user._id,
      isActive: true
    })
      .populate('participants', 'name email profileImage')
      .populate('admin', 'name email')
      .populate('lastMessage.sender', 'name')
      .sort({ 'metadata.lastActivity': -1 });

    // Calculate unread messages for each chat
    const chatsWithUnread = chats.map(chat => ({
      ...chat.toObject(),
      unreadCount: chat.unreadCount(req.user._id)
    }));

    res.json({ chats: chatsWithUnread });
  } catch (error) {
    console.error('Get chats error:', error);
    res.status(500).json({ message: 'Error fetching chats' });
  }
};

// Get single chat by ID
exports.getChatById = async (req, res) => {
  try {
    const chat = await Chat.findById(req.params.id)
      .populate('participants', 'name email profileImage')
      .populate('admin', 'name email')
      .populate('messages.sender', 'name email profileImage')
      .populate('messages.readBy.user', 'name');

    if (!chat) {
      return res.status(404).json({ message: 'Chat not found' });
    }

    // Check if user is participant
    if (!chat.participants.some(p => p._id.toString() === req.user._id.toString())) {
      return res.status(403).json({ message: 'Not authorized to access this chat' });
    }

    // Mark messages as read
    await chat.markAsRead(req.user._id);

    res.json({ chat });
  } catch (error) {
    console.error('Get chat error:', error);
    res.status(500).json({ message: 'Error fetching chat' });
  }
};

// Create new group chat
exports.createGroupChat = async (req, res) => {
  try {
    const { name, participants, description } = req.body;

    // Validate participants
    if (!participants || participants.length < 2) {
      return res.status(400).json({ message: 'Group chat must have at least 2 participants' });
    }

    // Create chat
    const chat = await Chat.create({
      type: 'group',
      name,
      description,
      participants: [...participants, req.user._id],
      admin: req.user._id
    });

    await chat.populate('participants', 'name email profileImage');
    await chat.populate('admin', 'name email');

    res.status(201).json({
      message: 'Group chat created successfully',
      chat
    });
  } catch (error) {
    console.error('Create group chat error:', error);
    res.status(500).json({ message: 'Error creating group chat' });
  }
};

// Get or create individual chat
exports.getOrCreateIndividualChat = async (req, res) => {
  try {
    const { userId } = req.params;

    // Validate user exists
    const otherUser = await User.findById(userId);
    if (!otherUser) {
      return res.status(404).json({ message: 'User not found' });
    }

    const chat = await Chat.getOrCreateIndividualChat(req.user._id, userId);
    await chat.populate('participants', 'name email profileImage');
    await chat.populate('messages.sender', 'name email profileImage');

    res.json({ chat });
  } catch (error) {
    console.error('Get/Create individual chat error:', error);
    res.status(500).json({ message: 'Error accessing chat' });
  }
};

// Send message in chat
exports.sendMessage = async (req, res) => {
  try {
    const { content, attachments } = req.body;
    const chat = await Chat.findById(req.params.id);

    if (!chat) {
      return res.status(404).json({ message: 'Chat not found' });
    }

    // Check if user is participant
    if (!chat.participants.includes(req.user._id)) {
      return res.status(403).json({ message: 'Not authorized to send message in this chat' });
    }

    const message = await chat.addMessage(req.user._id, content, attachments);
    await message.populate('sender', 'name email profileImage');

    // Here you would typically emit the message to connected socket clients
    // io.to(chat._id).emit('new_message', { chat: chat._id, message });

    res.json({
      message: 'Message sent successfully',
      chatMessage: message
    });
  } catch (error) {
    console.error('Send message error:', error);
    res.status(500).json({ message: 'Error sending message' });
  }
};

// Update group chat
exports.updateGroupChat = async (req, res) => {
  try {
    const { name, description } = req.body;
    const chat = await Chat.findById(req.params.id);

    if (!chat) {
      return res.status(404).json({ message: 'Chat not found' });
    }

    // Check if user is admin
    if (chat.admin.toString() !== req.user._id.toString()) {
      return res.status(403).json({ message: 'Only admin can update group chat' });
    }

    chat.name = name || chat.name;
    chat.description = description || chat.description;
    await chat.save();

    await chat.populate('participants', 'name email profileImage');
    await chat.populate('admin', 'name email');

    res.json({
      message: 'Group chat updated successfully',
      chat
    });
  } catch (error) {
    console.error('Update group chat error:', error);
    res.status(500).json({ message: 'Error updating group chat' });
  }
};

// Add participant to group chat
exports.addParticipant = async (req, res) => {
  try {
    const { userId } = req.body;
    const chat = await Chat.findById(req.params.id);

    if (!chat) {
      return res.status(404).json({ message: 'Chat not found' });
    }

    // Check if user is admin
    if (chat.admin.toString() !== req.user._id.toString()) {
      return res.status(403).json({ message: 'Only admin can add participants' });
    }

    // Check if user already in chat
    if (chat.participants.includes(userId)) {
      return res.status(400).json({ message: 'User already in chat' });
    }

    chat.participants.push(userId);
    await chat.save();

    await chat.populate('participants', 'name email profileImage');

    res.json({
      message: 'Participant added successfully',
      chat
    });
  } catch (error) {
    console.error('Add participant error:', error);
    res.status(500).json({ message: 'Error adding participant' });
  }
};

// Remove participant from group chat
exports.removeParticipant = async (req, res) => {
  try {
    const { userId } = req.params;
    const chat = await Chat.findById(req.params.id);

    if (!chat) {
      return res.status(404).json({ message: 'Chat not found' });
    }

    // Check if user is admin
    if (chat.admin.toString() !== req.user._id.toString()) {
      return res.status(403).json({ message: 'Only admin can remove participants' });
    }

    // Remove participant
    chat.participants = chat.participants.filter(p => p.toString() !== userId);
    await chat.save();

    await chat.populate('participants', 'name email profileImage');

    res.json({
      message: 'Participant removed successfully',
      chat
    });
  } catch (error) {
    console.error('Remove participant error:', error);
    res.status(500).json({ message: 'Error removing participant' });
  }
};

// Leave group chat
exports.leaveChat = async (req, res) => {
  try {
    const chat = await Chat.findById(req.params.id);

    if (!chat) {
      return res.status(404).json({ message: 'Chat not found' });
    }

    // Remove user from participants
    chat.participants = chat.participants.filter(p => p.toString() !== req.user._id.toString());

    // If user is admin, assign new admin if there are other participants
    if (chat.admin.toString() === req.user._id.toString() && chat.participants.length > 0) {
      chat.admin = chat.participants[0];
    }

    // If no participants left, mark chat as inactive
    if (chat.participants.length === 0) {
      chat.isActive = false;
    }

    await chat.save();

    res.json({
      message: 'Left chat successfully'
    });
  } catch (error) {
    console.error('Leave chat error:', error);
    res.status(500).json({ message: 'Error leaving chat' });
  }
};
