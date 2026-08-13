const apiConfig = {
  user: {
    login: '/user/login',
    register: '/user/register',
    resetPassword: '/user/resetPassword',
    getCurrUser: '/user/getCurrUser',
    getUserByUserId: '/user/getUserByUserId',
    updateUser: '/user/updateUser',
    updateAvatar: '/user/updateAvatar',
    sendCode: '/user/sendCode',
    sendForgetCode: '/user/sendForgetCode',
    forgotPassword: '/user/forgotPassword'
  },
  qq: {
    authorize: '/qq/authorize'
  },
  category: {
    getCategoryList: '/category/getCategoryList'
  },
  commodity: {
    getCommodityList: '/commodity/getCommodityList',
    getCommodityById: '/commodity/getCommodityById',
    addCommodity: '/commodity/addCommodity',
    uploadCommunityImg: '/commodity/uploadCommunityImg',
    updateStatus: '/commodity/updateStatus',
    delete: '/commodity/delete',
    search: '/commodity/search',
    incrementBrowse: '/commodity/incrementBrowse'
  },
  alipay: {
    pay: '/alipay/pay',
    payQrCode: '/alipay/precreate',
    query: '/alipay/query'
  },
  receivingAddress: {
    getReceivingAddressList: '/receivingAddress/getReceivingAddressList',
    addReceivingAddress: '/receivingAddress/addReceivingAddress',
    updateReceivingAddress: '/receivingAddress/update',
    deleteReceivingAddress: '/receivingAddress/delete'
  },
  order: {
    create: '/order/create',
    list: '/order/list',
    getById: '/order/{id}',
    updateStatus: '/order/updateStatus',
    logistics: '/order/logistics',
    sellerList: '/order/sellerList',
    ship: '/order/ship',
    cancel: '/order/cancel'
  },
  favorite: {
    toggle: '/favorite/toggle',
    isFavorited: '/favorite/isFavorited',
    list: '/favorite/list',
    count: '/favorite/count'
  },
  browseHistory: {
    record: '/browseHistory/record',
    list: '/browseHistory/list'
  },
  chat: {
    conversationList: '/chat/conversation/list',
    messageList: '/chat/message/list',
    sendMessage: '/chat/message/send',
    createConversation: '/chat/conversation/create',
    markRead: '/chat/conversation/read',
    deleteConversation: '/chat/conversation/delete',
    uploadImage: '/chat/upload'
  },
  ai: {
    customerServiceInit: '/ai/customerServiceInit',
    customerServiceChat: '/ai/customerServiceChat',
    getHistory: '/ai/getCurrSessionHistoryList',
    beautifyDescription: '/ai/beautifyDescription'
  },
  admin: {
    userList: '/admin/user/list',
    userRoles: '/admin/user/roles',
    resetPassword: '/admin/user/resetPassword',
    deleteUser: '/admin/user',
    roleList: '/admin/role/list',
    roleAll: '/admin/role/all',
    addRole: '/admin/role',
    updateRole: '/admin/role',
    deleteRole: '/admin/role',
    commodityList: '/commodity/admin/list',
    deleteCommodity: '/commodity/admin/delete',
    categoryList: '/category/admin/list',
    addCategory: '/category/admin/add',
    updateCategory: '/category/admin/update',
    deleteCategory: '/category/admin/delete',
    knowledgeList: '/knowledge/admin/list',
    addKnowledgeText: '/knowledge/admin/addText',
    uploadKnowledgePdf: '/knowledge/admin/uploadPdf',
    knowledgeChunkCheck: '/knowledge/admin/chunk/check',
    knowledgeChunkUpload: '/knowledge/admin/chunk/upload',
    knowledgeChunkMerge: '/knowledge/admin/chunk/merge',
    knowledgeVectorize: '/knowledge/admin/vectorize',
    updateKnowledge: '/knowledge/admin/update',
    deleteKnowledge: '/knowledge/admin/delete',
    orderList: '/order/admin/list',
    updateOrderStatus: '/order/admin/updateStatus',
    dashboardStats: '/admin/dashboard/stats',
    dashboardWeekOrders: '/admin/dashboard/weekOrders',
    dashboardMonthCommodities: '/admin/dashboard/monthCommodities'
  }
}

export default apiConfig
