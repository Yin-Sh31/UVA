<template>
  <div class="weather-management">
    <h2>地图+天气管理</h2>
    <div class="weather-content">
      <!-- 雷达拼图 -->
      <div class="map-section">
        <h3>雷达拼图</h3>
        <div v-if="radarImageUrl" class="weather-iframe-container">
          <iframe :src="radarImageUrl" frameborder="0" class="weather-iframe"></iframe>
        </div>
        <div v-else class="loading">
          <el-button type="primary" :loading="true">加载雷达图中...</el-button>
        </div>
      </div>

      <!-- 台风路径 -->
      <div class="map-section">
        <h3>台风路径</h3>
        <div v-if="typhoonImageUrl" class="weather-iframe-container">
          <iframe :src="typhoonImageUrl" frameborder="0" class="weather-iframe"></iframe>
        </div>
        <div v-else class="loading">
          <el-button type="primary" :loading="true">加载台风路径中...</el-button>
        </div>
      </div>
      
      <!-- 城市选择器 -->
      <div class="city-selector">
        <h3>城市选择</h3>
        <el-cascader
          v-model="selectedCity"
          :options="cityOptions"
          placeholder="选择省/市/县"
          @change="handleCityChange"
          @expand-change="handleExpandChange"
          :props="{ expandTrigger: 'click', lazy: true, lazyLoad: loadChildDistricts }"
          popper-class="city-cascader-dropdown"
        />
      </div>
      
      <!-- 天气信息 -->
      <div v-if="weatherData" class="weather-info">
        <h3>🌤️ 天气信息</h3>
        <div class="weather-card">
          <div class="weather-current">
          <div class="current-info">
            <h4>{{ getSelectedCityName() }}</h4>
            <p>{{ weatherData.now.text }}</p>
            <p class="temperature">{{ weatherData.now.temp }}°C</p>
            <p>更新时间: {{ weatherData.updateTime }}</p>
          </div>
                  <div class="weather-icon">
              <img :src="`https://static.qweather.net/weather_icon/${weatherData.now.icon}.png`" alt="天气图标">
            </div>
          </div>
          
          <!-- 预警信息 -->
          <div v-if="warningData && warningData.warning && warningData.warning.length > 0" class="warning-section">
            <h4>⚠️ 天气预警</h4>
            <div v-if="hasSevereWarning" class="severe-warning">严重预警！</div>
            <div class="warning-list">
              <div 
                v-for="warning in warningData.warning" 
                :key="warning.id" 
                class="warning-item"
              >
                <div class="warning-header">
                  <span class="warning-type">{{ warning.typeName }}</span>
                  <span 
                    class="warning-level" 
                    :style="{ backgroundColor: getWarningLevelColor(warning.level) }"
                  >{{ warning.level }}</span>
                </div>
                <div class="warning-content">{{ warning.text }}</div>
                <div class="warning-time">
                  发布时间: {{ warning.pubTime }}
                  <span v-if="warning.endTime"> 结束时间: {{ warning.endTime }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 天气指数 -->
        <div v-if="indicesData && indicesData.daily && indicesData.daily.length > 0" class="indices-section">
          <h4>📊 天气指数</h4>
          <div class="indices-grid">
            <div 
              v-for="index in indicesData.daily" 
              :key="index.type" 
              class="indices-item"
            >
              <span class="indices-name">{{ index.name }}</span>
              <span class="indices-value">{{ index.category }}</span>
              <span class="indices-desc">{{ index.text }}</span>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div v-if="hasSevereWarning" class="action-section">
          <el-button type="danger" @click="handleSetRestriction">🚫 设置飞手接单限制</el-button>
        </div>
      </div>
      
      <div v-else-if="loading" class="loading">
        <el-button type="primary" :loading="true">加载中...</el-button>
      </div>
      
      <div v-else class="empty-state">
        暂无天气数据
      </div>
    </div>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  name: 'WeatherManagement',
  data() {
    return {
      cityOptions: [],
      selectedCity: [],
      selectedCityName: '',
      weatherData: null,
      warningData: null,
      indicesData: null,
      loading: false,
      hasSevereWarning: false,
      qweatherToken: null,
      apiKey: 'b425c15091174a0a97bceb0f6e4e20f7',
      radarImageUrl: '',
      typhoonImageUrl: ''
    }
  },
  mounted() {
    this.loadCityList()
    this.getQweatherToken()
    this.loadWeatherImages()
  },
  methods: {

    
    // 使用腾讯地图API获取省份数据（分层加载）
    async loadCityList(retryCount = 0) {
      this.loading = true
      try {
        console.log('开始加载省份数据...');
        // 使用list接口获取全国省份
        const response = await fetch('/tencent/ws/district/v1/list?key=57JBZ-RG56B-WC5UB-JNFZD-6VQYO-QQFME');
        console.log('API响应状态:', response.status);
        
        // 获取响应文本
        const text = await response.text();
        console.log('API响应文本长度:', text.length);
        console.log('API响应文本内容:', text);
        
        // 尝试解析JSON
        let data;
        try {
          data = JSON.parse(text);
          console.log('省份数据响应:', data);
        } catch (parseError) {
          console.error('JSON解析失败:', parseError);
          console.error('响应文本:', text);
          this.cityOptions = [];
          this.loading = false;
          return;
        }
        
        if (data.status !== 0) {
          console.error('API 返回错误:', data);
          this.cityOptions = [];
          this.loading = false;
          return;
        }
        
        // data.result[0] 直接包含全国省份数组
        const provinces = data.result[0]
        if (provinces && provinces.length > 0) {
          // 转换为级联选择器需要的格式：{value, label}
          this.cityOptions = provinces.map(province => ({
            value: province.id,   // 将id转换为value
            label: province.name, // 将name转换为label
            leaf: false           // 表示有子级，需要懒加载
          }))
          console.log('省份数据:', this.cityOptions);
          // 默认选择第一个省份
          if (this.cityOptions.length > 0) {
            this.selectedCity = [this.cityOptions[0].value]
            // 延迟一下，确保数据已加载
            setTimeout(() => {
              const cityName = this.getSelectedCityName();
              if (cityName && cityName !== '请选择城市') {
                this.fetchWeatherByCity(cityName);
              }
            }, 100);
          }
        } else {
          console.error('省份数据为空');
          this.cityOptions = [];
        }
      } catch (error) {
        console.error('加载省份数据失败:', error);
        this.cityOptions = [];
      } finally {
        this.loading = false
      }
    },
    
    // 动态加载子级数据
    async loadChildDistricts(node, resolve, retryCount = 0) {
      console.log('=== 开始加载子级数据 ===');
      console.log('节点信息:', node);
      console.log('节点value:', node.value);
      console.log('节点data:', node.data);
      
      // 检查节点值是否存在，可能在node.data.value中
      const nodeValue = node.data ? node.data.value : node.value;
      console.log('提取的节点值:', nodeValue);
      
      if (!nodeValue) {
        console.error('节点值不存在:', node);
        resolve([]);
        return;
      }
      
      try {
        console.log('开始请求子级数据，省份ID:', nodeValue);
        const response = await fetch(`/tencent/ws/district/v1/getchildren?key=57JBZ-RG56B-WC5UB-JNFZD-6VQYO-QQFME&id=${nodeValue}`);
        console.log('API响应状态:', response.status);
        
        const text = await response.text();
        console.log('子级数据响应文本:', text);
        
        let data;
        try {
          data = JSON.parse(text);
          console.log('子级数据响应:', data);
        } catch (parseError) {
          console.error('JSON解析失败:', parseError);
          resolve([]);
          return;
        }
        
        if (data.status !== 0) {
          console.error('加载子级数据失败，状态码:', data.status, '消息:', data.message);
          resolve([]);
          return;
        }
        
        // 获取子级行政区划数据
        console.log('响应结果结构:', data.result);
        const districts = data.result[0]
        console.log('子级行政区数量:', districts ? districts.length : 0);
        
        if (districts && districts.length > 0) {
          const children = districts.map(district => ({
            value: district.id,   // 将id转换为value
            label: district.name, // 将name转换为label
            leaf: true            // 表示没有子级
          }))
          console.log('转换后的子级数据:', children);
          resolve(children)
        } else {
          console.log('子级数据为空');
          resolve([])
        }
      } catch (error) {
        console.error('加载子级数据失败:', error);
        resolve([]);
      }
    },
    // 获取和风天气API Key
    async getQweatherToken() {
      try {
        console.log('=== 使用和风天气API Key ===');
        // 使用API Key认证方式，直接返回API Key
        this.qweatherToken = this.apiKey;
        console.log('API Key获取成功:', this.qweatherToken);
      } catch (error) {
        console.error('获取API Key失败:', error);
      }
    },
    
    // 加载天气图片链接
    async loadWeatherImages() {
      try {
        // 获取雷达图链接
        const radarResponse = await fetch('/api/weather/radar', {
          headers: {
            'Authorization': 'Bearer admin123'
          }
        });
        const radarData = await radarResponse.json();
        console.log('雷达图API响应:', radarData);
        if (radarData.code === 200) {
          this.radarImageUrl = radarData.data;
          console.log('雷达图链接获取成功:', this.radarImageUrl);
        }
        
        // 获取台风路径图链接
        const typhoonResponse = await fetch('/api/weather/typhoon', {
          headers: {
            'Authorization': 'Bearer admin123'
          }
        });
        const typhoonData = await typhoonResponse.json();
        console.log('台风路径API响应:', typhoonData);
        if (typhoonData.code === 200) {
          this.typhoonImageUrl = typhoonData.data;
          console.log('台风路径链接获取成功:', this.typhoonImageUrl);
        }
      } catch (error) {
        console.error('获取天气图片链接失败:', error);
      }
    },
    
    // 通过城市名称获取和风天气LocationID并查询天气
    async fetchWeatherByCity(cityName) {
      if (!cityName) {
        console.error('城市名称无效');
        return;
      }

      try {
        this.loading = true;

        // 1. 使用城市名称调用和风天气的城市搜索 API
        //    注意：对城市名称进行编码，确保中文等特殊字符能被正确传递
        const searchUrl = `/qweather/geo/v2/city/lookup?location=${encodeURIComponent(cityName)}`;
        const searchRes = await fetch(searchUrl, {
          headers: { 'X-QW-Api-Key': this.apiKey }
        });
        const searchData = await searchRes.json();
        console.log('城市搜索响应数据:', searchData);

        // 2. 处理城市搜索 API 的响应
        //    这里需要注意检查 status code，和风天气的成功状态码是 '200'，而不是数字 200
        if (searchData.code !== '200' || !searchData.location || searchData.location.length === 0) {
          // 如果搜索失败，打印更详细的错误信息，方便排查
          console.error(`城市搜索失败: ${cityName}`, searchData);
          throw new Error(`未找到城市 "${cityName}" 的天气ID`);
        }

        // 取搜索结果中的第一个城市 LocationID
        const locationId = searchData.location[0].id;
        console.log(`成功获取城市: ${cityName}, LocationID: ${locationId}`);

        // 3. 使用获取到的 LocationID 查询实时天气
        const weatherUrl = `/qweather/v7/weather/now?location=${locationId}`;
        const weatherRes = await fetch(weatherUrl, {
          headers: { 'X-QW-Api-Key': this.apiKey }
        });
        const weatherData = await weatherRes.json();
        console.log('天气查询响应数据:', weatherData);

        if (weatherData.code === '200') {
          console.log('天气数据获取成功:', weatherData.now);
          console.log('天气图标代码:', weatherData.now.icon);
          console.log('完整天气数据:', weatherData);
          // 更新页面的天气数据
          this.weatherData = {
            code: weatherData.code,
            now: {
              text: weatherData.now.text,
              temp: weatherData.now.temp,
              icon: weatherData.now.icon
            },
            updateTime: new Date().toLocaleString('zh-CN')
          };
          console.log('更新后的weatherData:', this.weatherData);
          
          // 获取天气预警
          const warningUrl = `/qweather/v7/warning/now?location=${locationId}`;
          const warningRes = await fetch(warningUrl, {
            headers: { 'X-QW-Api-Key': this.apiKey }
          });
          const warningData = await warningRes.json();
          console.log('预警查询响应数据:', warningData);
          this.warningData = warningData;

          // 获取天气指数
          const indicesUrl = `/qweather/v7/indices/1d?location=${locationId}&type=1,2,3`;
          const indicesRes = await fetch(indicesUrl, {
            headers: { 'X-QW-Api-Key': this.apiKey }
          });
          const indicesData = await indicesRes.json();
          console.log('指数查询响应数据:', indicesData);
          this.indicesData = indicesData;

          // 检查是否有严重预警（黄色及以上）
          this.hasSevereWarning = this.warningData.warning && this.warningData.warning.some(warning => 
            warning.level === '黄色' || warning.level === '橙色' || warning.level === '红色'
          );
          
          console.log('天气数据加载成功');
        } else {
          console.error('天气API返回错误:', weatherData);
        }
      } catch (error) {
        console.error('获取天气数据失败:', error);
        this.$message.error('获取天气数据失败');
      } finally {
        this.loading = false;
      }
    },


    handleCityChange() {
      const cityName = this.getSelectedCityName();
      if (cityName && cityName !== '请选择城市') {
        this.fetchWeatherByCity(cityName);
      }
    },
    
    // 处理展开事件
    handleExpandChange(value) {
      console.log('展开事件:', value)
    },
    
    // 获取选中的城市名称（返回纯净的城市名，不包含斜杠）
    getSelectedCityName() {
      if (!this.selectedCity || this.selectedCity.length === 0) {
        return '请选择城市'
      }
      // 只返回最后一个选择的城市名称
      let options = this.cityOptions
      let lastCityName = ''
      
      for (let i = 0; i< this.selectedCity.length; i++) {
        const value = this.selectedCity[i]
        const option = options.find(opt =>opt.value === value)
        if (option) {
          lastCityName = option.label
          options = option.children || []
        }
      }
      return lastCityName
    },
    getWarningLevelColor(level) {
      const levelColors = {
        '蓝色': '#1E90FF',
        '黄色': '#FFD700',
        '橙色': '#FF8C00',
        '红色': '#FF4500'
      }
      return levelColors[level] || '#999999'
    },
    handleSetRestriction() {
      this.$confirm('检测到黄色或红色预警天气，确定要设置飞手接单限制吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message({
          type: 'success',
          message: '飞手接单限制已设置'
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消设置'
        })
      })
    }
  }
}
</script>

<style scoped>
.weather-management {
  padding: 20px;
}

.weather-management h2 {
  color: #333;
  margin-bottom: 20px;
}

.weather-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.map-section {
  margin-bottom: 30px;
}

.map-section h3 {
  color: #333;
  margin-bottom: 15px;
}

/* 天气iframe容器 */
.weather-iframe-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.weather-iframe {
  width: 100%;
  height: 600px;
  display: block;
}

.city-selector {
  margin-bottom: 30px;
}

.city-selector h3 {
  color: #333;
  margin-bottom: 15px;
}

/* 城市选择器样式 */
:deep(.city-cascader-dropdown) {
  max-height: 400px !important;
  overflow-y: auto !important;
  z-index: 9999 !important;
}

:deep(.city-cascader-dropdown .el-cascader-menu) {
  max-height: 380px !important;
  overflow-y: auto !important;
}

.weather-info h3 {
  color: #333;
  margin-bottom: 20px;
}

.weather-card {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.weather-current {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.current-info h4 {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.current-info p {
  margin: 5px 0;
  color: #666;
}

.temperature {
  font-size: 36px;
  font-weight: bold;
  color: #ff6b35;
}

.weather-icon img {
  width: 100px;
  height: 100px;
}

.warning-section {
  margin-bottom: 20px;
}

.warning-section h4 {
  color: #333;
  margin-bottom: 15px;
}

.severe-warning {
  background-color: #ff4d4f;
  color: white;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  font-weight: bold;
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.warning-item {
  background-color: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 8px;
  padding: 15px;
}

.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.warning-type {
  font-weight: bold;
  color: #ff6b35;
}

.warning-level {
  padding: 4px 12px;
  border-radius: 4px;
  color: white;
  font-size: 14px;
}

.warning-content {
  color: #666;
  margin-bottom: 10px;
}

.warning-time {
  font-size: 12px;
  color: #999;
}

.indices-section {
  margin-bottom: 20px;
}

.indices-section h4 {
  color: #333;
  margin-bottom: 15px;
}

.indices-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.indices-item {
  background-color: #f0f9eb;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  padding: 15px;
}

.indices-name {
  display: block;
  font-weight: bold;
  color: #52c41a;
  margin-bottom: 5px;
}

.indices-value {
  display: block;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.indices-desc {
  display: block;
  font-size: 12px;
  color: #666;
}

.action-section {
  margin-top: 20px;
  text-align: center;
}

.loading,
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #999;
}
</style>