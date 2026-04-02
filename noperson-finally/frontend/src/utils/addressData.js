// 中国地址数据（简化版，仅包含部分省市）
export const addressData = [
  {
    value: '110000',
    label: '北京市',
    children: [
      {
        value: '110100',
        label: '北京市',
        children: [
          { value: '110101', label: '东城区' },
          { value: '110102', label: '西城区' },
          { value: '110105', label: '朝阳区' },
          { value: '110106', label: '丰台区' },
          { value: '110107', label: '石景山区' },
          { value: '110108', label: '海淀区' }
        ]
      }
    ]
  },
  {
    value: '310000',
    label: '上海市',
    children: [
      {
        value: '310100',
        label: '上海市',
        children: [
          { value: '310101', label: '黄浦区' },
          { value: '310104', label: '徐汇区' },
          { value: '310105', label: '长宁区' },
          { value: '310106', label: '静安区' },
          { value: '310107', label: '普陀区' },
          { value: '310109', label: '虹口区' }
        ]
      }
    ]
  },
  {
    value: '440000',
    label: '广东省',
    children: [
      {
        value: '440100',
        label: '广州市',
        children: [
          { value: '440103', label: '荔湾区' },
          { value: '440104', label: '越秀区' },
          { value: '440105', label: '海珠区' },
          { value: '440106', label: '天河区' },
          { value: '440111', label: '白云区' }
        ]
      },
      {
        value: '441800',
        label: '清远市',
        children: [
          { value: '441802', label: '清城区' },
          { value: '441803', label: '清新区' },
          { value: '441821', label: '佛冈县' },
          { value: '441823', label: '阳山县' },
          { value: '441825', label: '连山壮族瑶族自治县' },
          { value: '441826', label: '连南瑶族自治县' },
          { value: '441881', label: '英德市' },
          { value: '441882', label: '连州市' }
        ]
      }
    ]
  },
  {
    value: '450000',
    label: '广西壮族自治区',
    children: [
      {
        value: '450100',
        label: '南宁市',
        children: [
          { value: '450102', label: '兴宁区' },
          { value: '450103', label: '青秀区' },
          { value: '450105', label: '江南区' }
        ]
      }
    ]
  }
];

// 将地址级联值转换为文本
export const formatAddress = (addressArray) => {
  if (!addressArray || addressArray.length === 0) {
    return '';
  }
  return addressArray.join(' ');
};

// 从完整地址文本中解析出级联值（简化版，实际应用中可能需要更复杂的解析）
export const parseAddress = (addressText) => {
  if (!addressText) {
    return [];
  }
  // 这里只是一个简单的示例，实际应用中可能需要根据addressData进行匹配
  return addressText.split(' ').filter(Boolean);
};