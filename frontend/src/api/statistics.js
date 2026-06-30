import request from './request'

// 对应后端 StatisticsController（基础路径 /api/statistics）
export const statisticsApi = {
  // GET /statistics/dashboard 首页概览卡片
  dashboard: () => request.get('/statistics/dashboard'),
  // GET /statistics/suppliers 供应商分布
  suppliers: () => request.get('/statistics/suppliers'),
  // GET /statistics/parts 零件分布
  parts: () => request.get('/statistics/parts'),
  // GET /statistics/inventory 库存状态分布
  inventory: () => request.get('/statistics/inventory'),
  // GET /statistics/monthly-trend?startDateParam=&endDateParam=
  monthlyTrend: (startDateParam, endDateParam) =>
    request.get('/statistics/monthly-trend', {
      params: { startDateParam, endDateParam }
    })
}
