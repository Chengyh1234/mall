"""
电商平台大数据生成脚本
生成 100,000 条 SPU 数据，以及对应的：
- spu_sale_attr_choice（SPU 选择的销售属性）
- sku（SKU 数据，笛卡尔积组合）
- sku_sale_attr_values（SKU 与销售属性值关联）
- spu_basic_attr_values（SPU 基本属性值）

使用方式：
  1. 确保已安装 mysql-connector-python: pip install mysql-connector-python
  2. 修改下方数据库连接配置（DB_CONFIG）
  3. 运行: python generate_data.py

注意：数据量较大，建议分批执行（可通过 BATCH_SIZE 和 MAX_SPU_COUNT 控制）
"""

import mysql.connector
import random
import itertools
import json
import time
from datetime import datetime
from collections import defaultdict

# ============================================================
# 数据库连接配置（请根据实际情况修改）
# ============================================================
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'ecommerce_platform',
    'charset': 'utf8mb4',
    'autocommit': False,
    'connection_timeout': 300,
    'allow_multi_queries': True,
}

# ============================================================
# 生成参数配置
# ============================================================
BATCH_SIZE = 100          # 每批插入 SPU 数量
MAX_SPU_COUNT = 100000    # 总共要生成的 SPU 数量
START_SPU_ID = 63         # spu 表当前 AUTO_INCREMENT 值

# 卖家/店铺映射
SELLER_STORE_MAP = [
    (2, 1),  # seller_id=2, store_id=1
    (3, 2),  # seller_id=3, store_id=2
]

# ============================================================
# 分类名称映射（从数据库 categories 表的记录推断）
# 用于生成 SPU 名称时的品类描述
# ============================================================
CATEGORY_NAMES = {
    27: '手机', 28: '手机配件', 29: '数码相机', 30: '耳机音响',
    31: '笔记本电脑', 32: '台式电脑', 33: '电脑外设', 34: '办公用品',
    35: '电视', 36: '冰箱', 37: '洗衣机', 38: '空调',
    39: '男装上衣', 40: '男装裤子', 41: '男装外套',
    42: '连衣裙', 43: '女装上衣', 44: '半身裙', 45: '女装裤子',
    46: '运动鞋', 47: '皮鞋', 48: '休闲鞋', 49: '凉鞋/拖鞋',
    50: '背包', 51: '钱包', 52: '饰品',
    53: '四件套', 54: '被芯', 55: '枕芯',
    56: '锅具', 57: '刀具砧板', 58: '餐具',
    59: '坚果炒货', 60: '饼干糕点', 61: '巧克力糖果',
    62: '白酒', 63: '啤酒', 64: '饮料',
    65: '华硕电脑', 66: '短袖', 67: '短裤',
    69: '大米',
    12: '智能手表', 11: '智能手环', 13: '智能家居',
    120: '洁面', 121: '爽肤水', 122: '面霜', 123: '精华',
    124: '粉底液', 125: '口红', 126: '眼影',
    127: '男士香水', 128: '女士香水',
    129: '跑步鞋', 130: '运动T恤', 131: '冲锋衣',
    132: '帐篷', 133: '登山包', 134: '睡袋',
    135: '哑铃', 136: '瑜伽垫',
    137: '科幻小说', 138: '历史传记', 139: '投资理财', 140: '职场励志',
    141: '绘本', 142: '科普百科',
    143: '轮胎', 144: '刹车片',
    145: '机油', 146: '玻璃水',
    147: '行车记录仪', 148: '车载充电器',
    149: '狗粮', 150: '猫粮', 151: '宠物罐头', 152: '宠物零食棒',
    153: '猫砂', 154: '宠物窝垫',
}

# 商品名称前缀池（按分类）
SPU_NAME_PREFIX = {
    27: ['旗舰手机', '智能5G手机', '全面屏手机', '拍照手机', '游戏手机', '超长续航手机'],
    28: ['手机壳', '手机贴膜', '充电器', '数据线', '手机支架', '移动电源'],
    29: ['数码相机', '微单相机', '单反相机', '拍立得', '运动相机'],
    30: ['蓝牙耳机', '头戴式耳机', '入耳式耳机', '无线音箱', '智能音箱'],
    31: ['轻薄本', '游戏本', '商务本', '创作本', '办公笔记本', '高性能笔记本'],
    32: ['台式电脑', 'DIY主机', '办公电脑', '游戏主机', '家用电脑', '一体机'],
    33: ['机械键盘', '无线鼠标', '显示器', '摄像头', 'U盘', '移动硬盘'],
    34: ['打印机', '碎纸机', '装订机', '投影仪', '办公文具'],
    35: ['智能电视', '4K超清电视', '全面屏电视', '量子点电视', 'OLED电视', 'MiniLED电视'],
    36: ['双门冰箱', '三门冰箱', '对开门冰箱', '十字对开门冰箱', '法式多门冰箱', '迷你冰箱'],
    37: ['滚筒洗衣机', '波轮洗衣机', '洗烘一体机', '迷你洗衣机', '壁挂洗衣机'],
    38: ['壁挂式空调', '立柜式空调', '中央空调', '移动空调', '窗式空调'],
    39: ['休闲T恤', '商务衬衫', 'Polo衫', '打底衫', '针织衫', '卫衣'],
    40: ['休闲长裤', '牛仔裤', '西裤', '运动裤', '短裤', '工装裤'],
    41: ['休闲西装', '夹克外套', '风衣', '羽绒服', '棉服', '冲锋衣'],
    42: ['碎花连衣裙', '纯色连衣裙', '蕾丝连衣裙', '衬衫连衣裙', '针织连衣裙', '雪纺连衣裙'],
    43: ['女装T恤', '女装衬衫', '雪纺衫', '针织衫', '女装卫衣'],
    44: ['A字裙', '百褶裙', '包臀裙', '牛仔裙', '纱裙', '半身长裙'],
    45: ['女装牛仔裤', '女装休闲裤', '打底裤', '阔腿裤', '瑜伽裤'],
    46: ['运动鞋', '跑步鞋', '篮球鞋', '休闲运动鞋', '训练鞋'],
    47: ['商务皮鞋', '休闲皮鞋', '正装皮鞋', '英伦皮鞋'],
    48: ['帆布鞋', '板鞋', '豆豆鞋', '乐福鞋', '休闲布鞋'],
    49: ['凉鞋', '拖鞋', '洞洞鞋', '沙滩鞋', '人字拖'],
    50: ['双肩背包', '单肩包', '电脑包', '旅行包', '休闲包'],
    51: ['短款钱包', '长款钱包', '折叠钱包', '卡包', '零钱包'],
    52: ['项链', '手链', '耳环', '戒指', '手镯', '发饰'],
    53: ['纯棉四件套', '磨毛四件套', '天丝四件套', '水洗棉四件套'],
    54: ['羽绒被', '蚕丝被', '棉被', '纤维被', '大豆纤维被'],
    55: ['乳胶枕', '记忆枕', '羽绒枕', '荞麦枕', '决明子枕'],
    56: ['炒锅', '汤锅', '不粘锅', '铸铁锅', '砂锅', '蒸锅'],
    57: ['切片刀', '斩骨刀', '厨师刀', '水果刀', '砧板', '菜板'],
    58: ['碗', '盘子', '筷子', '餐具套装', '马克杯', '玻璃杯'],
    59: ['每日坚果', '混合坚果', '夏威夷果', '开心果', '巴旦木', '腰果'],
    60: ['曲奇饼干', '苏打饼干', '夹心饼干', '威化饼干', '蛋糕', '面包'],
    61: ['纯巧克力', '夹心巧克力', '巧克力礼盒', '糖果', '棒棒糖', '软糖'],
    62: ['酱香型白酒', '浓香型白酒', '清香型白酒', '年份白酒'],
    63: ['精酿啤酒', '工业啤酒', '黑啤', '白啤', '果啤'],
    64: ['碳酸饮料', '果汁', '茶饮料', '功能饮料', '矿泉水', '牛奶'],
    65: ['华硕笔记本', '华硕游戏本', '华硕轻薄本', '华硕办公本'],
    66: ['纯棉短袖', '潮流短袖T恤', '印花短袖', '基础款短袖', '运动短袖'],
    67: ['休闲短裤', '运动短裤', '牛仔短裤', '工装短裤', '百慕大短裤'],
    69: ['东北大米', '珍珠米', '长粒香米', '五常大米', '有机大米'],
    12: ['智能手表', '运动手表', '商务手表', '儿童手表', '健康手表'],
    11: ['智能手环', '运动手环', '健康手环', '心率手环'],
    13: ['智能插座', '智能灯', '智能门锁', '智能窗帘', '智能摄像头'],
    120: ['洁面乳', '洗面奶', '洁面泡沫', '卸妆水', '卸妆油'],
    121: ['爽肤水', '柔肤水', '收敛水', '精华水', '喷雾'],
    122: ['面霜', '日霜', '晚霜', '保湿霜', '修护霜'],
    123: ['精华液', '精华露', '精华油', '安瓶精华', '肌底液'],
    124: ['粉底液', '气垫粉底', '粉底霜', '遮瑕膏', 'BB霜'],
    125: ['口红', '唇膏', '唇釉', '唇彩', '染唇液'],
    126: ['眼影盘', '单色眼影', '液体眼影', '眼影笔'],
    127: ['男士淡香水', '男士古龙水', '男士香氛', '运动香水'],
    128: ['女士淡香水', '女士浓香水', '少女香水', '花香调香水'],
    129: ['缓震跑鞋', '竞速跑鞋', '减震跑步鞋', '轻量跑鞋', '马拉松跑鞋'],
    130: ['运动T恤', '速干T恤', '运动背心', '训练T恤', '跑步T恤'],
    131: ['冲锋衣', '软壳冲锋衣', '硬壳冲锋衣', '三合一冲锋衣'],
    132: ['自动帐篷', '手搭帐篷', '家庭帐篷', '露营帐篷', '速开帐篷'],
    133: ['登山包', '户外背包', '徒步背包', '骑行背包', '攀岩背包'],
    134: ['羽绒睡袋', '棉睡袋', '信封式睡袋', '木乃伊睡袋'],
    135: ['固定哑铃', '可调节哑铃', '哑铃套装', '浸塑哑铃', '包胶哑铃'],
    136: ['加厚瑜伽垫', '防滑瑜伽垫', '初学者瑜伽垫', 'TPE瑜伽垫', 'NBR瑜伽垫'],
    137: ['科幻小说', '科幻文学', '星际科幻', '科幻经典', '科幻系列'],
    138: ['历史传记', '人物传记', '历史通俗读物', '中国历史', '世界历史'],
    139: ['投资入门', '理财指南', '股票投资', '基金定投', '价值投资'],
    140: ['职场励志', '成功励志', '自我提升', '时间管理', '沟通技巧'],
    141: ['儿童绘本', '启蒙绘本', '睡前故事', '亲子阅读', '获奖绘本'],
    142: ['儿童科普', '自然百科', '动物百科', '科学启蒙', 'DK百科'],
    143: ['轿车轮胎', 'SUV轮胎', '冬季轮胎', '静音轮胎', '耐磨轮胎'],
    144: ['陶瓷刹车片', '半金属刹车片', '低金属刹车片', 'NAO刹车片'],
    145: ['全合成机油', '半合成机油', '矿物机油', '柴油机油'],
    146: ['防冻玻璃水', '夏季玻璃水', '浓缩玻璃水', '0°C玻璃水'],
    147: ['流媒体后视镜', '隐藏式记录仪', '4K记录仪', '前后双录记录仪'],
    148: ['快充车载充电器', '无线车载充电器', '多口车载充电器'],
    149: ['成犬狗粮', '幼犬狗粮', '天然狗粮', '无谷狗粮', '冻干狗粮'],
    150: ['成猫猫粮', '幼猫猫粮', '天然猫粮', '无谷猫粮', '室内猫粮'],
    151: ['狗罐头', '猫罐头', '主食罐头', '零食罐头', '汤罐头'],
    152: ['狗咬胶', '磨牙棒', '鸡肉干', '宠物饼干', '洁齿骨'],
    153: ['豆腐猫砂', '膨润土猫砂', '混合猫砂', '松木猫砂', '水晶猫砂'],
    154: ['宠物窝', '宠物垫', '猫窝', '狗窝', '宠物床'],
}

# 品牌映射（按分类）
CATEGORY_BRANDS = {
    27: [1, 2, 3, 4, 7, 8, 9],          # 手机：华为、苹果、小米、三星、OPPO、vivo、荣耀
    31: [1, 10, 11],                     # 笔记本：华为、联想、戴尔
    32: [10, 1],                         # 台式机：联想、华为
    35: [3, 12, 13],                     # 电视：小米、海尔、美的
    36: [12, 13, 14],                    # 冰箱：海尔、美的、格力
    37: [12, 13],                        # 洗衣机：海尔、美的
    38: [14, 13, 12],                    # 空调：格力、美的、海尔
    39: [],                              # 上衣：无品牌
    40: [],                              # 裤子：无品牌
    41: [],                              # 外套：无品牌
    42: [],                              # 连衣裙：无品牌
    43: [],                              # 女装上衣：无品牌
    44: [],                              # 半身裙：无品牌
    45: [],                              # 女装裤子：无品牌
    46: [5, 6, 15, 16],                  # 运动鞋：耐克、阿迪达斯、安踏、李宁
    47: [],                              # 皮鞋：无品牌
    48: [],                              # 休闲鞋：无品牌
    49: [],                              # 凉鞋/拖鞋：无品牌
    50: [],                              # 背包：无品牌
    51: [],                              # 钱包：无品牌
    52: [],                              # 饰品：无品牌
    53: [],                              # 四件套：无品牌
    54: [],                              # 被芯：无品牌
    55: [],                              # 枕芯：无品牌
    56: [],                              # 锅具：无品牌
    57: [],                              # 刀具砧板：无品牌
    58: [],                              # 餐具：无品牌
    59: [],                              # 坚果炒货：无品牌
    60: [],                              # 饼干糕点：无品牌
    61: [],                              # 巧克力糖果：无品牌
    62: [],                              # 白酒：无品牌
    63: [],                              # 啤酒：无品牌
    64: [],                              # 饮料：无品牌
    65: [10],                            # 华硕电脑：联想
    66: [],                              # 短袖：无品牌
    67: [],                              # 短裤：无品牌
    69: [],                              # 大米：无品牌
    12: [1, 2, 3],                       # 智能手表：华为、苹果、小米
    11: [1, 3],                          # 智能手环：华为、小米
    13: [3, 12],                         # 智能家居：小米、海尔
    120: [],                             # 洁面：无品牌
    121: [],                             # 爽肤水：无品牌
    122: [],                             # 面霜：无品牌
    123: [],                             # 精华：无品牌
    124: [],                             # 粉底液：无品牌
    125: [],                             # 口红：无品牌
    126: [],                             # 眼影：无品牌
    127: [],                             # 男士香水：无品牌
    128: [],                             # 女士香水：无品牌
    129: [5, 6, 15, 16],                 # 跑步鞋：耐克、阿迪达斯、安踏、李宁
    130: [5, 6, 15, 16],                 # 运动T恤：耐克、阿迪达斯、安踏、李宁
    131: [5, 6, 15, 16],                 # 冲锋衣：耐克、阿迪达斯、安踏、李宁
    132: [],                             # 帐篷：无品牌
    133: [],                             # 登山包：无品牌
    134: [],                             # 睡袋：无品牌
    135: [],                             # 哑铃：无品牌
    136: [],                             # 瑜伽垫：无品牌
    137: [],                             # 科幻小说：无品牌
    138: [],                             # 历史传记：无品牌
    139: [],                             # 投资理财：无品牌
    140: [],                             # 职场励志：无品牌
    141: [],                             # 绘本：无品牌
    142: [],                             # 科普百科：无品牌
    143: [],                             # 轮胎：无品牌
    144: [],                             # 刹车片：无品牌
    145: [],                             # 机油：无品牌
    146: [],                             # 玻璃水：无品牌
    147: [],                             # 行车记录仪：无品牌
    148: [],                             # 车载充电器：无品牌
    149: [],                             # 狗粮：无品牌
    150: [],                             # 猫粮：无品牌
    151: [],                             # 宠物罐头：无品牌
    152: [],                             # 宠物零食棒：无品牌
    153: [],                             # 猫砂：无品牌
    154: [],                             # 宠物窝垫：无品牌
}

# 单位映射
CATEGORY_UNIT = {
    27: '台', 28: '个', 29: '台', 30: '个',
    31: '台', 32: '台', 33: '个', 34: '台',
    35: '台', 36: '台', 37: '台', 38: '台',
    39: '件', 40: '条', 41: '件', 42: '件',
    43: '件', 44: '条', 45: '条',
    46: '双', 47: '双', 48: '双', 49: '双',
    50: '个', 51: '个', 52: '件',
    53: '套', 54: '条', 55: '个',
    56: '口', 57: '把', 58: '套',
    59: '袋', 60: '袋', 61: '盒',
    62: '瓶', 63: '瓶', 64: '瓶',
    65: '台', 66: '件', 67: '条',
    69: '袋',
    12: '只', 11: '个', 13: '个',
    120: '支', 121: '瓶', 122: '瓶', 123: '瓶',
    124: '瓶', 125: '支', 126: '盘',
    127: '瓶', 128: '瓶',
    129: '双', 130: '件', 131: '件',
    132: '个', 133: '个', 134: '条',
    135: '对', 136: '张',
    137: '本', 138: '本', 139: '本', 140: '本',
    141: '本', 142: '本',
    143: '条', 144: '套',
    145: '瓶', 146: '瓶',
    147: '台', 148: '个',
    149: '袋', 150: '袋', 151: '罐', 152: '盒',
    153: '袋', 154: '个',
}


def get_connection():
    """获取数据库连接"""
    return mysql.connector.connect(**DB_CONFIG)


def fetch_reference_data(cursor):
    """
    获取所有引用数据：
    - category_attributes: 分类与属性关联
    - attributes: 属性定义
    - attribute_values: 属性值
    - brands: 品牌
    - stores: 店铺
    """
    print("[INFO] 开始获取引用数据...")

    # 获取分类与属性关联
    cursor.execute("""
        SELECT ca.category_id, ca.attr_id, a.attr_type, a.name as attr_name
        FROM category_attributes ca
        JOIN attributes a ON a.id = ca.attr_id
        ORDER BY ca.category_id, a.attr_type, ca.sort
    """)
    cat_attr_rows = cursor.fetchall()
    
    category_attrs = defaultdict(lambda: {'sale': [], 'basic': []})
    for cat_id, attr_id, attr_type, attr_name in cat_attr_rows:
        if attr_type == 1:
            category_attrs[cat_id]['sale'].append(attr_id)
        else:
            category_attrs[cat_id]['basic'].append(attr_id)

    # 获取属性值
    cursor.execute("""
        SELECT av.id, av.attr_id, av.value, av.seller_id
        FROM attribute_values av
        WHERE av.seller_id IS NULL
        ORDER BY av.attr_id, av.sort
    """)
    attr_value_rows = cursor.fetchall()
    
    attr_values = defaultdict(list)
    for val_id, attr_id, value, seller_id in attr_value_rows:
        attr_values[attr_id].append({
            'id': val_id,
            'value': value,
            'seller_id': seller_id
        })

    # 获取品牌
    cursor.execute("SELECT id, name FROM brands WHERE status = 1 AND is_deleted = 0")
    brands = cursor.fetchall()
    brand_map = {b[0]: b[1] for b in brands}

    # 获取店铺
    cursor.execute("SELECT id, seller_id FROM stores WHERE status = 1")
    stores = cursor.fetchall()

    print(f"[INFO]   - 分类数: {len(category_attrs)}")
    print(f"[INFO]   - 属性值数: {sum(len(v) for v in attr_values.values())}")
    print(f"[INFO]   - 品牌数: {len(brand_map)}")
    print(f"[INFO]   - 店铺数: {len(stores)}")

    return dict(category_attrs), attr_values, brand_map, stores


def get_available_categories(category_attrs):
    """获取有完整属性的分类（至少有销售属性和基本属性）"""
    available = []
    for cat_id, attrs in category_attrs.items():
        if attrs['sale'] and attrs['basic']:
            available.append(cat_id)
    return sorted(available)


def pick_sale_attr_values(attr_id, attr_values, max_count=4):
    """为销售属性选取 2~max_count 个值"""
    values = attr_values.get(attr_id, [])
    # 过滤掉卖家自定义的值（seller_id is not null 的通常是卖家自定义）
    platform_values = [v for v in values if v['seller_id'] is None]
    if not platform_values:
        platform_values = values
    if len(platform_values) < 2:
        return platform_values
    count = min(random.randint(2, max_count), len(platform_values))
    return random.sample(platform_values, count)


def pick_basic_attr_value(attr_id, attr_values, manual_prob=0.3):
    """
    为基本属性选取值。
    有 30% 概率使用手动输入值，70% 概率从平台预设值中选取。
    """
    values = attr_values.get(attr_id, [])
    platform_values = [v for v in values if v['seller_id'] is None]
    
    if not platform_values:
        return None, f"标准值_{attr_id}"
    
    if random.random() < manual_prob:
        # 手动输入
        sample = random.choice(platform_values)
        return None, f"{sample['value']}(增强版)"
    else:
        # 从预设值中选取
        chosen = random.choice(platform_values)
        return chosen['id'], None


def generate_spu_name(category_id, brand_id, brand_map, index):
    """生成 SPU 商品名称"""
    prefixes = SPU_NAME_PREFIX.get(category_id, ['商品'])
    prefix = random.choice(prefixes)
    
    if brand_id and brand_id in brand_map:
        brand_name = brand_map[brand_id]
        return f"{brand_name}{prefix} {index}"
    else:
        return f"{prefix} {index}"


def generate_spu_description(category_id, brand_id, brand_map):
    """生成 SPU 描述"""
    features = ['高品质', '超值', '热销', '新品', '爆款', '经典款', '升级版']
    feature = random.choice(features)
    
    if brand_id and brand_id in brand_map:
        brand_name = brand_map[brand_id]
        return f"{brand_name}品牌{feature}商品，品质保证，值得信赖"
    return f"{feature}商品，品质保证，值得信赖"


def generate_min_price(category_id):
    """
    根据分类生成一个合理的底价范围。
    不同品类有不同的价格区间。
    """
    base_prices = {
        27: 1999.00, 28: 19.00, 29: 1999.00, 30: 99.00,
        31: 3999.00, 32: 2999.00, 33: 49.00, 34: 299.00,
        35: 1999.00, 36: 1499.00, 37: 1299.00, 38: 1999.00,
        39: 59.00, 40: 89.00, 41: 199.00, 42: 129.00,
        43: 59.00, 44: 79.00, 45: 89.00,
        46: 299.00, 47: 199.00, 48: 99.00, 49: 29.00,
        50: 99.00, 51: 49.00, 52: 19.00,
        53: 199.00, 54: 99.00, 55: 39.00,
        56: 59.00, 57: 29.00, 58: 19.00,
        59: 29.00, 60: 9.00, 61: 19.00,
        62: 99.00, 63: 5.00, 64: 3.00,
        65: 4999.00, 66: 39.00, 67: 49.00,
        69: 19.00,
        12: 299.00, 11: 99.00, 13: 49.00,
        120: 29.00, 121: 49.00, 122: 59.00, 123: 89.00,
        124: 59.00, 125: 39.00, 126: 49.00,
        127: 99.00, 128: 99.00,
        129: 199.00, 130: 59.00, 131: 299.00,
        132: 99.00, 133: 99.00, 134: 99.00,
        135: 29.00, 136: 19.00,
        137: 19.00, 138: 19.00, 139: 29.00, 140: 19.00,
        141: 15.00, 142: 29.00,
        143: 299.00, 144: 49.00,
        145: 49.00, 146: 9.00,
        147: 199.00, 148: 29.00,
        149: 29.00, 150: 29.00, 151: 9.00, 152: 9.00,
        153: 19.00, 154: 29.00,
    }
    base = base_prices.get(category_id, 99.00)
    # 随机波动 ±20%
    base *= random.uniform(0.8, 1.2)
    return round(base, 2)


def price_for_category(category_id):
    """根据分类返回价格区间 (min, max)"""
    price_ranges = {
        27: (899, 8999), 28: (9, 299), 29: (999, 9999), 30: (29, 1999),
        31: (2999, 12999), 32: (1999, 9999), 33: (19, 999), 34: (99, 2999),
        35: (1299, 9999), 36: (899, 5999), 37: (699, 4999), 38: (1599, 7999),
        39: (29, 599), 40: (49, 499), 41: (99, 1999), 42: (59, 899),
        43: (29, 599), 44: (39, 499), 45: (49, 499),
        46: (99, 1999), 47: (99, 999), 48: (49, 599), 49: (9, 199),
        50: (29, 599), 51: (19, 299), 52: (5, 199),
        53: (99, 999), 54: (49, 599), 55: (19, 199),
        56: (19, 399), 57: (9, 199), 58: (5, 199),
        59: (9, 199), 60: (5, 99), 61: (9, 199),
        62: (29, 999), 63: (3, 29), 64: (1, 19),
        65: (3999, 15999), 66: (19, 299), 67: (29, 399),
        69: (9, 99),
        12: (99, 3999), 11: (49, 999), 13: (19, 599),
        120: (9, 199), 121: (19, 299), 122: (29, 399), 123: (49, 599),
        124: (29, 399), 125: (19, 299), 126: (19, 399),
        127: (49, 599), 128: (49, 599),
        129: (99, 1499), 130: (29, 399), 131: (149, 1999),
        132: (49, 999), 133: (49, 699), 134: (49, 599),
        135: (9, 299), 136: (9, 199),
        137: (9, 59), 138: (9, 59), 139: (19, 69), 140: (9, 49),
        141: (9, 39), 142: (19, 69),
        143: (199, 1999), 144: (29, 199),
        145: (19, 399), 146: (5, 29),
        147: (99, 999), 148: (19, 199),
        149: (9, 199), 150: (9, 199), 151: (5, 29), 152: (5, 39),
        153: (9, 59), 154: (19, 199),
    }
    min_p, max_p = price_ranges.get(category_id, (99, 999))
    return min_p, max_p


def main():
    start_time = time.time()
    print("=" * 60)
    print("  电商平台大数据生成脚本")
    print(f"  目标: 生成 {MAX_SPU_COUNT} 条 SPU 数据")
    print("=" * 60)

    # 连接数据库
    conn = get_connection()
    cursor = conn.cursor(buffered=True)

    try:
        # 获取引用数据
        category_attrs, attr_values, brand_map, stores = fetch_reference_data(cursor)
        available_categories = get_available_categories(category_attrs)
        
        print(f"\n[INFO] 可用分类数: {len(available_categories)}")
        print(f"[INFO] 可用分类: {available_categories}")
        
        if not available_categories:
            print("[ERROR] 没有可用分类！请先配置 category_attributes 数据")
            return

        # 统计变量
        total_spu = 0
        total_sku = 0
        total_choice = 0
        total_basic_attr = 0
        total_sku_attr_value = 0
        batch_start_time = time.time()

        # 主循环：分批生成 SPU
        for batch_start in range(START_SPU_ID, START_SPU_ID + MAX_SPU_COUNT, BATCH_SIZE):
            batch_end = min(batch_start + BATCH_SIZE, START_SPU_ID + MAX_SPU_COUNT)
            batch_spu_count = batch_end - batch_start

            # ============================================================
            # 收集本批数据
            # ============================================================
            batch_spu = []
            batch_choice = []
            batch_sku = []
            batch_basic_attr = []

            for spu_index in range(batch_start, batch_end):
                # 1. 随机选取分类
                category_id = random.choice(available_categories)
                attrs = category_attrs[category_id]
                sale_attr_ids = attrs['sale']
                basic_attr_ids = attrs['basic']

                # 2. 选取品牌
                cat_brands = CATEGORY_BRANDS.get(category_id, [])
                brand_id = random.choice(cat_brands) if cat_brands else None

                # 3. 选取卖家/店铺
                seller_id, store_id = random.choice(SELLER_STORE_MAP)

                # 4. 生成 SPU 名称
                spu_name = generate_spu_name(category_id, brand_id, brand_map, spu_index)

                # 5. 生成 min_price
                min_price = generate_min_price(category_id)

                # 6. 生成销量
                sales = random.randint(0, 5000)

                # 7. 生成描述
                description = generate_spu_description(category_id, brand_id, brand_map)

                # 8. 生成 SPU 记录
                unit = CATEGORY_UNIT.get(category_id, '个')
                now = datetime.now()
                batch_spu.append((
                    spu_name, category_id, brand_id, seller_id, store_id,
                    min_price, sales, description, unit,
                    1, 0, now, now
                ))

                # ============================================================
                # 处理销售属性 (spu_sale_attr_choice + sku + sku_sale_attr_values)
                # ============================================================
                # 每个销售属性选择 2-3 个值
                sale_choices = {}  # attr_id -> list of value_ids
                sale_choice_values = {}  # attr_id -> list of value dicts
                
                for sale_attr_id in sale_attr_ids:
                    max_vals = 3 if len(sale_attr_ids) >= 2 else 4
                    chosen = pick_sale_attr_values(sale_attr_id, attr_values, max_vals)
                    if len(chosen) >= 2:
                        sale_choices[sale_attr_id] = [v['id'] for v in chosen]
                        sale_choice_values[sale_attr_id] = chosen

                # 至少要有一个销售属性组合
                if not sale_choices:
                    # 如果没有足够的销售属性，尝试只用一个
                    for sale_attr_id in sale_attr_ids[:1]:
                        chosen = pick_sale_attr_values(sale_attr_id, attr_values, 3)
                        if len(chosen) >= 2:
                            sale_choices[sale_attr_id] = [v['id'] for v in chosen]
                            sale_choice_values[sale_attr_id] = chosen
                            break

                if not sale_choices:
                    continue  # 跳过这个 SPU

                # 记录 spu_sale_attr_choice（暂存，需要等 SPU 插入后才能获取 spu_id）
                # 这里先保存，后面通过 spu_index 关联
                temp_choices = []
                for attr_id, val_ids in sale_choices.items():
                    temp_choices.append((attr_id, val_ids))
                batch_choice.append((spu_index, temp_choices))

                # ============================================================
                # 生成 SKU（笛卡尔积）
                # ============================================================
                attr_ids_list = list(sale_choices.keys())
                value_ids_list = [sale_choices[aid] for aid in attr_ids_list]
                
                # 如果笛卡尔积太大，限制 max 12 个 SKU
                cartesian = list(itertools.product(*value_ids_list))
                if len(cartesian) > 12:
                    cartesian = random.sample(cartesian, 12)

                min_p, max_p = price_for_category(category_id)
                # 为这个 SPU 生成一个基础价格
                base_price = random.uniform(min_p, max_p)
                base_price = round(base_price / 10) * 10

                temp_skus = []
                for combo in cartesian:
                    # 价格波动
                    variation = random.uniform(0.85, 1.15)
                    price = round(base_price * variation, 2)
                    market_price = round(price * random.uniform(1.05, 1.25), 2)
                    cost_price = round(price * random.uniform(0.5, 0.75), 2)
                    stock = random.randint(50, 1000)
                    temp_skus.append((price, market_price, cost_price, stock, 1, 0, now, now, combo))

                batch_sku.append((spu_index, temp_skus, attr_ids_list))

                # ============================================================
                # 处理基本属性 (spu_basic_attr_values)
                # ============================================================
                temp_basic = []
                for basic_attr_id in basic_attr_ids:
                    val_id, manual_val = pick_basic_attr_value(basic_attr_id, attr_values)
                    temp_basic.append((basic_attr_id, val_id, manual_val))
                batch_basic_attr.append((spu_index, temp_basic))

            # ============================================================
            # 插入本批数据
            # ============================================================
            try:
                # ---- 1. 批量插入 SPU ----
                spu_sql = """INSERT INTO spu 
                    (name, category_id, brand_id, seller_id, store_id, 
                     min_price, sales, description, unit,
                     status, is_deleted, created_at, updated_at) 
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
                cursor.executemany(spu_sql, batch_spu)
                conn.commit()

                # 获取刚插入的 SPU ID 范围
                cursor.execute("SELECT LAST_INSERT_ID()")
                first_spu_id = cursor.fetchone()[0]
                inserted_spu_ids = list(range(first_spu_id, first_spu_id + len(batch_spu)))

                # ---- 2. 批量插入 spu_sale_attr_choice ----
                choice_sql = """INSERT INTO spu_sale_attr_choice 
                    (spu_id, attr_id, selected_values, created_at, updated_at) 
                    VALUES (%s, %s, %s, %s, %s)"""
                choice_data = []
                for idx, (_, choices) in enumerate(batch_choice):
                    spu_id = inserted_spu_ids[idx]
                    for attr_id, val_ids in choices:
                        choice_data.append((
                            spu_id, attr_id, json.dumps(val_ids), now, now
                        ))
                if choice_data:
                    cursor.executemany(choice_sql, choice_data)
                    conn.commit()

                # ---- 3. 批量插入 SKU ----
                sku_sql = """INSERT INTO sku 
                    (spu_id, price, market_price, cost_price, stock, 
                     status, is_deleted, created_at, updated_at) 
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)"""
                sku_data = []
                
                # Track which spu_index maps to which combo
                for idx, (orig_idx, temp_skus, attr_ids) in enumerate(batch_sku):
                    spu_id = inserted_spu_ids[idx]
                    for combo_idx, sku_info in enumerate(temp_skus):
                        price, market_price, cost_price, stock, status, is_deleted, created_at, updated_at, combo = sku_info
                        sku_data.append((
                            spu_id, price, market_price, cost_price, stock,
                            status, is_deleted, created_at, updated_at
                        ))
                
                if sku_data:
                    cursor.executemany(sku_sql, sku_data)
                    conn.commit()

                    # 获取刚插入的 SKU ID 范围
                    cursor.execute("SELECT LAST_INSERT_ID()")
                    first_sku_id = cursor.fetchone()[0]
                    
                    # ---- 4. 批量插入 sku_sale_attr_values ----
                    attr_val_sql = """INSERT INTO sku_sale_attr_values 
                        (sku_id, attr_value_id, created_at, updated_at) 
                        VALUES (%s, %s, %s, %s)"""
                    attr_val_data = []
                    
                    sku_idx = 0
                    for idx, (orig_idx, temp_skus, attr_ids) in enumerate(batch_sku):
                        for combo_idx, sku_info in enumerate(temp_skus):
                            sku_id = first_sku_id + sku_idx
                            combo = sku_info[8]  # the combo tuple
                            for val_id in combo:
                                attr_val_data.append((
                                    sku_id, val_id, now, now
                                ))
                            sku_idx += 1
                    
                    if attr_val_data:
                        # 分批插入 sku_sale_attr_values（可能很多）
                        sub_batch_size = 5000
                        for i in range(0, len(attr_val_data), sub_batch_size):
                            sub_batch = attr_val_data[i:i + sub_batch_size]
                            cursor.executemany(attr_val_sql, sub_batch)
                            conn.commit()
                        total_sku_attr_value += len(attr_val_data)

                # ---- 5. 批量插入 spu_basic_attr_values ----
                basic_sql = """INSERT INTO spu_basic_attr_values 
                    (spu_id, attr_id, attr_value_id, manual_value, created_at, updated_at) 
                    VALUES (%s, %s, %s, %s, %s, %s)"""
                basic_data = []
                for idx, (orig_idx, temp_basic) in enumerate(batch_basic_attr):
                    spu_id = inserted_spu_ids[idx]
                    for attr_id, val_id, manual_val in temp_basic:
                        basic_data.append((
                            spu_id, attr_id, val_id, manual_val, now, now
                        ))
                if basic_data:
                    cursor.executemany(basic_sql, basic_data)
                    conn.commit()

                # 更新统计
                total_spu += len(batch_spu)
                total_sku += len(sku_data)
                total_choice += len(choice_data)
                total_basic_attr += len(basic_data)

                # 进度输出
                elapsed = time.time() - batch_start_time
                spu_per_sec = total_spu / elapsed if elapsed > 0 else 0
                remaining = (MAX_SPU_COUNT - total_spu) / spu_per_sec if spu_per_sec > 0 else 0
                
                print(f"[PROGRESS] SPU: {total_spu:>6}/{MAX_SPU_COUNT}  "
                      f"SKU: {total_sku:>6}  "
                      f"速度: {spu_per_sec:.0f} SPU/s  "
                      f"预计剩余: {remaining:.0f}s")

            except mysql.connector.Error as err:
                print(f"[ERROR] 批次插入失败: {err}")
                conn.rollback()
                continue

        # 最终统计
        total_time = time.time() - start_time
        print("\n" + "=" * 60)
        print(f"  生成完成!")
        print(f"  耗时: {total_time:.2f}s ({total_time/60:.1f}分钟)")
        print(f"  SPU: {total_spu}")
        print(f"  spu_sale_attr_choice: {total_choice}")
        print(f"  SKU: {total_sku}")
        print(f"  sku_sale_attr_values: {total_sku_attr_value}")
        print(f"  spu_basic_attr_values: {total_basic_attr}")
        print("=" * 60)

    finally:
        cursor.close()
        conn.close()


if __name__ == '__main__':
    main()