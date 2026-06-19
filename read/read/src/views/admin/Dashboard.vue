<template>
  <div class="admin-dashboard">
    <!-- 统计卡片（不变） -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">总用户数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.noteCount }}</div>
            <div class="stat-label">笔记总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon"><el-icon><ChatLineSquare /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.commentCount }}</div>
            <div class="stat-label">评论总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="stat-card">
          <div class="stat-icon"><el-icon><View /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.viewCount }}</div>
            <div class="stat-label">总浏览量</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第一行图表：用户增长 & 笔记发布 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" header="用户增长趋势">
          <div ref="userChart" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" header="笔记发布趋势">
          <div ref="noteChart" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行图表：评论量趋势 & 浏览量趋势（新增） -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" header="评论量趋势">
          <div ref="commentChart" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" header="浏览量趋势">
          <div ref="viewChart" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import * as echarts from 'echarts'
import {ChatLineSquare, Document, User, View} from '@element-plus/icons-vue'
import {getAdminStats, getCommentTrend, getViewTrend,getUserTrend, getNoteTrend} from '@/api/modules/admin'

const stats = ref({
  userCount: 0,
  noteCount: 0,
  commentCount: 0,
  viewCount: 0
})

const userChart = ref(null)
const noteChart = ref(null)
const commentChart = ref(null)
const viewChart = ref(null)

const loadStats = async () => {
  try {
    stats.value = await getAdminStats()
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}
const initCharts = async () => {
  // 用户增长趋势（从后端获取）
  try {
    const userRes = await getUserTrend()
    const userChartInstance = echarts.init(userChart.value)
    userChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: userRes.months },
      yAxis: { type: 'value', name: '新增用户' },
      series: [{ type: 'line', data: userRes.data, smooth: true, lineStyle: { color: '#409EFF' } }]
    })
  } catch (error) {
    console.error('加载用户增长趋势失败', error)
  }

  // 笔记发布趋势（从后端获取）
  try {
    const noteRes = await getNoteTrend()
    const noteChartInstance = echarts.init(noteChart.value)
    noteChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: noteRes.months },
      yAxis: { type: 'value', name: '新增笔记' },
      series: [{ type: 'bar', data: noteRes.data, itemStyle: { color: '#67C23A' } }]
    })
  } catch (error) {
    console.error('加载笔记发布趋势失败', error)
  }

  // 评论量趋势和浏览量趋势（保持原有从后端获取）
  try {
    const commentRes = await getCommentTrend()
    const commentChartInstance = echarts.init(commentChart.value)
    commentChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: commentRes.months },
      yAxis: { type: 'value', name: '评论数' },
      series: [{ type: 'line', data: commentRes.data, smooth: true, lineStyle: { color: '#E6A23C' } }]
    })
  } catch (error) {
    console.error('加载评论趋势失败', error)
  }

  try {
    const viewRes = await getViewTrend()
    const viewChartInstance = echarts.init(viewChart.value)
    viewChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: viewRes.months },
      yAxis: { type: 'value', name: '浏览量' },
      series: [{ type: 'line', data: viewRes.data, smooth: true, lineStyle: { color: '#F56C6C' } }]
    })
  } catch (error) {
    console.error('加载浏览量趋势失败', error)
  }
}

onMounted(() => {
  loadStats()
  initCharts()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 50px;
  height: 50px;
  background: #ecf5ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #409EFF;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.chart-card {
  border-radius: 12px;
}
</style>