const socketIO = require('socket.io');
const jwt = require('jsonwebtoken');
const User = require('../models/user.model');

let io;

// Initialize socket.io
exports.init = (server) => {
  io = socketIO(server, {
    cors: {
      origin: "*", // Configure according to your frontend URL in production
      methods: ["GET", "POST"]
    }
  });

  // Authentication middleware
  io.use(async (socket, next) => {
    try {
      const token = socket.handshake.auth.token;
      if (!token) {
        return next(new Error('Authentication error'));
      }

      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      const user = await User.findById(decoded.id);

      if (!user) {
        return next(new Error('User not found'));
      }

      socket.user = user;
      next();
    } catch (error) {
      next(new Error('Authentication error'));
    }
  });

  // Connection handler
  io.on('connection', (socket) => {
    console.log(`User connected: ${socket.user._id}`);

    // Join personal room for private messages
    socket.join(socket.user._id.toString());

    // Join chat room
    socket.on('join_chat', (chatId) => {
      socket.join(chatId);
      console.log(`User ${socket.user._id} joined chat: ${chatId}`);
    });

    // Leave chat room
    socket.on('leave_chat', (chatId) => {
      socket.leave(chatId);
      console.log(`User ${socket.user._id} left chat: ${chatId}`);
    });

    // New message handler
    socket.on('send_message', async (data) => {
      const { chatId, message } = data;
      
      // Emit message to all users in the chat room
      io.to(chatId).emit('new_message', {
        chatId,
        message: {
          ...message,
          sender: {
            _id: socket.user._id,
            name: socket.user.name
          }
        }
      });
    });

    // Typing indicator
    socket.on('typing_start', (chatId) => {
      socket.to(chatId).emit('user_typing', {
        chatId,
        user: {
          _id: socket.user._id,
          name: socket.user.name
        }
      });
    });

    socket.on('typing_end', (chatId) => {
      socket.to(chatId).emit('user_stopped_typing', {
        chatId,
        user: {
          _id: socket.user._id,
          name: socket.user.name
        }
      });
    });

    // Read receipts
    socket.on('mark_read', (data) => {
      const { chatId, messageIds } = data;
      socket.to(chatId).emit('messages_read', {
        chatId,
        messageIds,
        userId: socket.user._id
      });
    });

    // Online status
    socket.on('set_status', (status) => {
      io.emit('user_status_change', {
        userId: socket.user._id,
        status
      });
    });

    // Disconnect handler
    socket.on('disconnect', () => {
      console.log(`User disconnected: ${socket.user._id}`);
      io.emit('user_status_change', {
        userId: socket.user._id,
        status: 'offline'
      });
    });
  });

  return io;
};

// Get socket.io instance
exports.getIO = () => {
  if (!io) {
    throw new Error('Socket.io not initialized');
  }
  return io;
};

// Emit event to specific user
exports.emitToUser = (userId, event, data) => {
  io.to(userId.toString()).emit(event, data);
};

// Emit event to specific chat room
exports.emitToChat = (chatId, event, data) => {
  io.to(chatId.toString()).emit(event, data);
};

// Emit event to all connected clients
exports.emitToAll = (event, data) => {
  io.emit(event, data);
};
