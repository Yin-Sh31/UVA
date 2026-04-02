/**
 * 系统枚举定义
 */

// 需求状态枚举
const SprayDemandStatusEnum = {
  // 待接取
  WAITING: {
    value: 0,
    label: '待接取',
    color: '#FF9800'
  },
  // 处理中
  PROCESSING: {
    value: 1,
    label: '处理中',
    color: '#2196F3'
  },
  // 作业中
  IN_OPERATION: {
    value: 2,
    label: '作业中',
    color: '#4CAF50'
  },
  // 待确认
  PENDING_CONFIRM: {
    value: 3,
    label: '待确认',
    color: '#FFC107'
  },
  // 已完成
  COMPLETED: {
    value: 4,
    label: '已完成',
    color: '#9E9E9E'
  },
  // 已取消
  CANCELLED: {
    value: 5,
    label: '已取消',
    color: '#F44336'
  }
};

// 根据状态值获取状态对象
const getSprayDemandStatusByValue = (value) => {
  for (const key in SprayDemandStatusEnum) {
    if (SprayDemandStatusEnum[key].value === value) {
      return SprayDemandStatusEnum[key];
    }
  }
  return {
    value: value,
    label: '未知状态',
    color: '#9E9E9E'
  };
};

// 根据状态值获取状态标签
const getSprayDemandStatusLabel = (value) => {
  return getSprayDemandStatusByValue(value).label;
};

// 根据状态值获取状态颜色
const getSprayDemandStatusColor = (value) => {
  return getSprayDemandStatusByValue(value).color;
};

// 用户角色枚举
const UserRoleEnum = {
  FARMER: 'farmer',
  FLYER: 'flyer',
  ADMIN: 'admin'
};

// 需求类型枚举
const DemandTypeEnum = {
  SPRAY: {
    value: 1,
    label: '喷洒需求'
  },
  INSPECTION: {
    value: 2,
    label: '巡检需求'
  }
};

// 设备类型枚举
const DeviceTypeEnum = {
  DRONE: {
    value: 1,
    label: '无人机'
  },
  GROUND: {
    value: 2,
    label: '地面设备'
  }
};

// 作业类型枚举
const WorkTypeEnum = {
  SPRAY: {
    value: 1,
    label: '喷洒'
  },
  INSPECTION: {
    value: 2,
    label: '巡检'
  },
  OTHER: {
    value: 3,
    label: '其他'
  }
};

// 订单支付状态枚举
const PaymentStatusEnum = {
  UNPAID: {
    value: 1,
    label: '未支付'
  },
  PAID: {
    value: 2,
    label: '已支付'
  },
  REFUNDED: {
    value: 3,
    label: '已退款'
  },
  PARTIAL_REFUND: {
    value: 4,
    label: '部分退款'
  }
};

module.exports = {
  SprayDemandStatusEnum,
  getSprayDemandStatusByValue,
  getSprayDemandStatusLabel,
  getSprayDemandStatusColor,
  UserRoleEnum,
  DemandTypeEnum,
  DeviceTypeEnum,
  WorkTypeEnum,
  PaymentStatusEnum
};