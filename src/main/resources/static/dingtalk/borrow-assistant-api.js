// API-backed implementation for the V1.5 H5 page.
// Keeps the original UI contract from 直播借样助手H5.html while persisting through Spring Boot/MySQL.

var S = {
  user: 'zhangsan',
  tab: 'pending',
  pFilter: 'all',
  bFilter: 'normal',
  searching: false,
  stack: ['page-main'],
  detailId: null,
  retShop: '',
  retMethod: 'express',
  retItems: [],
  retStoreId: '',
  retStoreName: '',
  batchNo: '',
  allocs: [],
  allocMethod: ''
};

var CACHE = {
  users: [],
  shops: [],
  stores: [],
  tasks: {},
  details: {},
  batches: {}
};

var PENDING_REQUESTS = 0;

function setBusy(isBusy) {
  PENDING_REQUESTS = Math.max(0, PENDING_REQUESTS + (isBusy ? 1 : -1));
  var busy = PENDING_REQUESTS > 0;
  document.body.classList.toggle('is-busy', busy);
  document.querySelectorAll('button').forEach(function(btn) {
    if (btn.classList.contains('search-clear-btn')) return;
    btn.disabled = busy;
    btn.setAttribute('aria-disabled', busy ? 'true' : 'false');
  });
}

function apiGet(url) {
  return apiFetch(url);
}

function apiPost(url, body) {
  return apiFetch(url, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: body === undefined ? undefined : JSON.stringify(body)
  });
}

function apiFetch(url, options) {
  var controller = window.AbortController ? new AbortController() : null;
  var timer = controller ? setTimeout(function() { controller.abort(); }, 15000) : null;
  var init = options || {};
  if (controller) init.signal = controller.signal;
  setBusy(true);
  return fetch(url, init)
    .then(readApiResponse)
    .catch(function(err) {
      if (err && err.name === 'AbortError') throw new Error('请求超时，请稍后重试');
      throw err;
    })
    .finally(function() {
      if (timer) clearTimeout(timer);
      setBusy(false);
    });
}

function fetchTaskDetail(taskId) {
  return apiGet('/api/v1/borrow/tasks/' + encodeURIComponent(taskId)).then(function(detail) {
    CACHE.details[taskId] = detail;
    CACHE.tasks[taskId] = detailToUiTask(detail);
    return CACHE.tasks[taskId];
  });
}

function readApiResponse(response) {
  return response.json().then(function(json) {
    if (!response.ok || json.code !== '0') {
      throw new Error(json.message || response.statusText || '请求失败');
    }
    return json.data;
  });
}

function escapeHtml(value) {
  return String(value == null ? '' : value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function toUiTask(task) {
  var status = mapTaskStatus(task.taskStatus, task.deliveryType, task.returnStatus);
  var activeReturnBatchNo = task.returnStatus && task.returnStatus !== 'NONE' ? (task.currentReturnBatchNo || '') : '';
  return {
    taskId: task.taskNo,
    type: task.deliveryType === 'PICKUP' ? 'pick_up' : 'express',
    status: status,
    userId: task.applicantEmpId || 'zhangsan',
    virtualShopId: task.virtualStoreCode,
    virtualShopName: task.virtualStoreName || task.virtualStoreCode,
    items: summaryToItems(task.itemSummary),
    applyTime: task.expectedReturnAt || '',
    borrowDate: task.expectedReturnAt ? task.expectedReturnAt.slice(0, 10) : '',
    dueDate: task.expectedReturnAt ? task.expectedReturnAt.slice(0, 10) : '',
    logisticsNo: task.logisticsNo || '',
    logisticsCompany: task.logisticsNo ? '' : '',
    pickupStoreId: '',
    pickupStoreName: task.deliveryType === 'PICKUP' ? (task.sourceStoreName || '') : '',
    pickupStoreAddress: '',
    gmsTransferNo: '',
    gmsReturnTransferNo: '',
    returnBatchNo: activeReturnBatchNo,
    returnMethod: '',
    returnStatus: mapReturnStatus(task.returnStatus),
    returnedQty: 0,
    returnLogisticsNo: task.returnStatus === 'LOGISTICS_FILLED' ? (task.logisticsNo || '') : '',
    returnLogisticsCompany: '',
    returnStoreId: '',
    returnStoreName: task.returnStatus === 'STORE_PENDING' ? (task.sourceStoreName || '') : '',
    receiverName: '',
    receiverPhone: '',
    receiverAddress: '',
    raw: task
  };
}

function detailToUiTask(detail) {
  var task = toUiTask({
    taskNo: detail.taskNo,
    borrowNo: detail.borrowNo,
    virtualStoreCode: detail.virtualStoreCode,
    virtualStoreName: detail.virtualStoreName,
    sourceStoreName: detail.sourceStoreName,
    deliveryType: detail.deliveryType,
    taskStatus: detail.taskStatus,
    deliveryStatus: detail.deliveryStatus,
    pickupStatus: detail.pickupStatus,
    returnStatus: detail.returnStatus,
    expectedReturnAt: detail.expectedReturnAt,
    logisticsNo: detail.logisticsNo,
    itemSummary: ''
  });
  task.items = (detail.items || []).map(function(item) {
    return {
      taskItemId: item.taskItemId,
      sku: item.skuCode,
      size: item.sizeCode,
      productName: item.productName || item.skuCode,
      qty: item.approvedQty,
      picked: item.pickedQty || item.receivedQty || 0,
      returned: item.returnedApplyQty || 0,
      borrowingQty: item.borrowingQty || 0
    };
  });
  task.applyTime = detail.borrowedAt || '';
  task.borrowDate = detail.borrowedAt ? detail.borrowedAt.slice(0, 10) : '';
  task.dueDate = detail.expectedReturnAt ? detail.expectedReturnAt.slice(0, 10) : '';
  task.pickupStoreId = detail.pickupStoreCode || '';
  task.pickupStoreName = detail.pickupStoreName || task.pickupStoreName || '';
  task.receiverName = detail.receiverName || '';
  task.receiverPhone = detail.receiverMobile || '';
  task.receiverAddress = detail.receiverFullAddress || '';
  task.returnBatchNo = detail.returnStatus && detail.returnStatus !== 'NONE' ? (detail.currentReturnBatchNo || '') : '';
  task.rawDetail = detail;
  return task;
}

function mapTaskStatus(taskStatus, deliveryType, returnStatus) {
  if (taskStatus === 'WAIT_SHIP') return 'pending';
  if (taskStatus === 'IN_TRANSIT') return 'in_transit';
  if (taskStatus === 'WAIT_PICKUP') return 'pending_pickup';
  if (taskStatus === 'BORROWING') return 'borrowing';
  if (taskStatus === 'RETURNING') {
    if (returnStatus === 'PENDING') return 'return_pending';
    if (returnStatus === 'LOGISTICS_FILLED') return 'return_logistics_filled';
    if (returnStatus === 'STORE_PENDING') return 'store_pending';
    return deliveryType === 'PICKUP' ? 'store_pending' : 'return_pending';
  }
  if (taskStatus === 'COMPLETED' || taskStatus === 'CLOSED') return 'completed';
  return String(taskStatus || '').toLowerCase();
}

function mapReturnStatus(status) {
  var map = {
    NONE: '',
    PENDING: 'return_pending',
    LOGISTICS_FILLED: 'return_logistics_filled',
    STORE_PENDING: 'store_pending',
    COMPLETED: 'completed'
  };
  return map[status] || '';
}

function summaryToItems(summary) {
  if (!summary) return [];
  var match = String(summary).match(/^(.+?)\((.+?)\)x(\d+)/);
  if (!match) return [{sku: summary, size: '', qty: 1, picked: 0, returned: 0}];
  return [{sku: match[1], size: match[2].replace('码', ''), qty: Number(match[3]), picked: 0, returned: 0}];
}

function getUser() {
  for (var i = 0; i < CACHE.users.length; i++) if (CACHE.users[i].userId === S.user) return CACHE.users[i];
  return CACHE.users[0] || { userId: 'zhangsan', userName: '张三', virtualShopIds: [] };
}

function getShop(id) {
  for (var i = 0; i < CACHE.shops.length; i++) if (CACHE.shops[i].virtualShopId === id) return CACHE.shops[i];
  return null;
}

function getStore(id) {
  for (var i = 0; i < CACHE.stores.length; i++) if (CACHE.stores[i].storeId === id) return CACHE.stores[i];
  return null;
}

function isOverdue(t) {
  if (!t.dueDate) return false;
  var d = new Date(t.dueDate), n = new Date();
  n.setHours(0,0,0,0);
  return n > d;
}
function overdueDays(t) { if (!t.dueDate) return 0; return Math.max(0, Math.floor((new Date() - new Date(t.dueDate)) / 864e5)); }
function remainDays(t) { if (!t.dueDate) return -1; var d = new Date(t.dueDate), n = new Date(); n.setHours(0,0,0,0); return Math.max(0, Math.floor((d - n) / 864e5)); }
function fmtDate(d) { return d ? String(d).slice(0, 10) : '-'; }

function userTasks() {
  var u = getUser(), allowed = u.virtualShopIds || [], r = {};
  Object.keys(CACHE.tasks).forEach(function(id) {
    var task = CACHE.tasks[id];
    if (!allowed.length || allowed.indexOf(task.virtualShopId) >= 0) r[id] = task;
  });
  return r;
}

function itemDesc(items) {
  if (!items || !items.length) return '无商品';
  var total = 0; items.forEach(function(i) { total += Number(i.qty || 0); });
  return items[0].sku + ' ' + items[0].size + ' x' + items[0].qty + (items.length > 1 ? ' 等' : '') + ' 共' + total + '件';
}

function statusInfo(status, task) {
  var m = {
    pending: {t:'待发货',i:'🔷',c:'status-pending'}, in_transit: {t:'发货在途',i:'🚚',c:'status-in-transit'},
    pending_pickup: {t:'待自提',i:'📦',c:'status-pending-pickup'}, borrowing: {t:'借样中',i:'🔵',c:'status-borrowing'},
    return_pending: {t:'待归还',i:'🟡',c:'status-return-pending'}, return_logistics_filled: {t:'物流已填',i:'🟢',c:'status-logistics-filled'},
    store_pending: {t:'待门店收货',i:'🟣',c:'status-store-pending'}, completed: {t:'已完结',i:'✅',c:'status-completed'}
  };
  var info = m[status] || {t:status,i:'',c:''};
  if (status === 'borrowing' && task && isOverdue(task)) return {t:'逾期待还',i:'🔴',c:'status-overdue'};
  return info;
}

function refreshTasks() {
  return apiGet('/api/v1/borrow/tasks?pageNo=1&pageSize=500').then(function(data) {
    CACHE.tasks = {};
    (data.list || []).forEach(function(task) {
      var ui = toUiTask(task);
      CACHE.tasks[ui.taskId] = ui;
    });
    renderMain();
    enableKeyboardActivation();
  });
}

function navigateTo(pid) {
  document.querySelectorAll('.page').forEach(function(p) { p.classList.remove('active'); });
  document.getElementById(pid).classList.add('active');
  if (S.stack[S.stack.length - 1] !== pid) S.stack.push(pid);
  window.scrollTo(0, 0);
  if (pid === 'page-main') refreshTasks();
  if (pid === 'page-borrow') initBorrow();
  if (pid === 'page-return') initReturn();
}
function goBack() {
  if (S.stack.length > 1) {
    S.stack.pop();
    var prev = S.stack[S.stack.length - 1];
    document.querySelectorAll('.page').forEach(function(p) { p.classList.remove('active'); });
    document.getElementById(prev).classList.add('active');
    window.scrollTo(0, 0);
    if (prev === 'page-main') refreshTasks();
  } else navigateTo('page-main');
}

function initUserSelector() {
  var sel = document.getElementById('user-select'); sel.innerHTML = '';
  CACHE.users.forEach(function(u) {
    var o = document.createElement('option'); o.value = u.userId; o.textContent = u.userName + ' (' + u.userId + ')'; sel.appendChild(o);
  });
  sel.value = S.user;
}
function onUserChange() {
  S.user = document.getElementById('user-select').value;
  S.pFilter = 'all'; S.bFilter = 'normal'; S.searching = false;
  clearSearch(); renderMain();
}

function renderMain() { renderPending(); renderBorrowing(); renderHistory(); }

function switchMainTab(tab) {
  S.tab = tab;
  document.querySelectorAll('#main-tab-bar .tab-item').forEach(function(el) { el.classList.toggle('active', el.getAttribute('data-tab') === tab); });
  document.getElementById('content-pending').classList.toggle('active', tab === 'pending');
  document.getElementById('content-borrowing').classList.toggle('active', tab === 'borrowing');
  document.getElementById('content-history').classList.toggle('active', tab === 'history');
  if (tab === 'pending') renderPending(); if (tab === 'borrowing') renderBorrowing(); if (tab === 'history') renderHistory();
}

function setPendingFilter(f) {
  S.pFilter = f;
  var pills = document.querySelectorAll('#filter-pending .filter-pill');
  var map = {all:0,pending:1,pending_pickup:2,in_transit:3};
  pills.forEach(function(p, i) { p.classList.toggle('active', i === map[f]); });
  renderPending();
}
function setBorrowingFilter(f) {
  S.bFilter = f;
  var pills = document.querySelectorAll('#filter-borrowing .filter-pill');
  var map = {all:0,normal:1,overdue:2};
  pills.forEach(function(p, i) { p.classList.toggle('active', i === map[f]); });
  renderBorrowing();
}

function renderPending() {
  var tasks = userTasks(), c = document.getElementById('pending-cards'), html = '', count = 0;
  for (var id in tasks) {
    var t = tasks[id];
    if (['pending','in_transit','pending_pickup'].indexOf(t.status) === -1) continue;
    if (S.pFilter !== 'all' && t.status !== S.pFilter) continue;
    count++; html += taskCard(id, t);
  }
  c.innerHTML = html || '<div class="empty-state"><div class="empty-icon"><i class="fa-solid fa-clipboard-list"></i></div>暂无待收货任务</div>';
  document.getElementById('pending-stat').textContent = '待收货：' + count + '个任务';
}

function renderBorrowing() {
  var tasks = userTasks(), c = document.getElementById('borrowing-cards'), html = '', count = 0;
  var borrowingStatuses = ['borrowing', 'return_pending', 'return_logistics_filled', 'store_pending'];
  for (var id in tasks) {
    var t = tasks[id];
    if (borrowingStatuses.indexOf(t.status) === -1) continue;
    var od = (t.status === 'borrowing' && isOverdue(t));
    if (S.bFilter === 'normal' && od) continue;
    if (S.bFilter === 'overdue' && !od) continue;
    count++; html += taskCard(id, t);
  }
  c.innerHTML = html || '<div class="empty-state"><div class="empty-icon"><i class="fa-solid fa-box-open"></i></div>暂无借样中任务</div>';
  document.getElementById('borrowing-stat').textContent = '借样中：' + count + '个任务';
}

function renderHistory() {
  var tasks = userTasks(), c = document.getElementById('history-cards'), html = '', count = 0;
  for (var id in tasks) { if (tasks[id].status !== 'completed') continue; count++; html += taskCard(id, tasks[id]); }
  c.innerHTML = html || '<div class="empty-state"><div class="empty-icon"><i class="fa-solid fa-clock-rotate-left"></i></div>暂无历史任务</div>';
  document.getElementById('history-stat').textContent = '历史记录：' + count + '个任务';
}

function taskCard(taskId, task) {
  var si = statusInfo(task.status, task), od = (task.status === 'borrowing' && isOverdue(task));
  var cls = od ? 'card card-overdue' : 'card';
  var lines = '<div>' + itemDesc(task.items) + '</div>';
  if (['pending','in_transit','pending_pickup'].indexOf(task.status) >= 0) lines += '<div>申请时间：' + fmtDate(task.applyTime) + '</div>';
  if (task.borrowDate && task.status !== 'pending') lines += '<div>借出日：' + task.borrowDate + '</div>';
  if (task.status === 'borrowing' && task.dueDate) {
    if (od) lines += '<div>预计归还：' + task.dueDate + ' <span class="red-text">超期' + overdueDays(task) + '天</span></div>';
    else lines += '<div>预计归还：' + task.dueDate + ' 剩余' + remainDays(task) + '天</div>';
  }
  if (task.type === 'pick_up' && task.pickupStoreName) lines += '<div>自提门店：' + task.pickupStoreName + '</div>';
  if (task.status === 'in_transit' && task.logisticsNo) lines += '<div style="color:#fa8c16"><i class="fa-solid fa-truck"></i> 物流：' + task.logisticsNo + '</div>';
  if (task.status === 'completed' && task.completedTime) lines += '<div>完结日：' + fmtDate(task.completedTime) + '</div>';
  if (task.returnBatchNo) lines += '<div style="color:#faad14;font-size:12px"><i class="fa-solid fa-rotate-left"></i> 归还批次：' + task.returnBatchNo + '</div>';
  if (task.returnLogisticsNo) lines += '<div style="color:#13c2c2;font-size:12px"><i class="fa-solid fa-truck"></i> 归还物流：' + task.returnLogisticsNo + '</div>';
  if (task.returnStoreName) lines += '<div style="color:#722ed1;font-size:12px"><i class="fa-solid fa-store"></i> 归还门店：' + task.returnStoreName + '</div>';

  var act = '<button class="btn btn-link btn-sm" onclick="viewDetail(\'' + taskId + '\')">查看详情</button>';
  if (task.status === 'pending') act += '<button class="btn btn-outline btn-sm" onclick="simulateShip(\'' + taskId + '\')">模拟发货</button>';
  else if (task.status === 'in_transit') act += '<button class="btn btn-primary btn-sm" onclick="confirmReceive(\'' + taskId + '\')">确认收货</button>';
  else if (task.status === 'pending_pickup') act += '<button class="btn btn-primary btn-sm" onclick="openPickupModal(\'' + taskId + '\')">确认自提</button>';
  else if (task.status === 'borrowing') act += '<button class="btn btn-primary btn-sm" onclick="returnFromTask(\'' + taskId + '\')">发起归还</button>';
  else if (task.status === 'return_pending') act += '<button class="btn btn-primary btn-sm" onclick="goToLogisticsForTask(\'' + taskId + '\')">填写物流单号</button>';
  else if (task.status === 'return_logistics_filled') act += '<button class="btn btn-outline btn-sm" onclick="simulateExpressComplete(\'' + taskId + '\')">模拟仓库收货完成</button>';
  else if (task.status === 'store_pending') act += '<button class="btn btn-success btn-sm" onclick="simulateStoreReceive(\'' + taskId + '\')">模拟门店收货</button>';

  return '<div class="' + cls + '"><div class="card-title"><span class="status-tag ' + si.c + '">' + si.t + si.i + '</span><span style="font-size:12px;color:#999">' + taskId + '</span></div><div class="card-content">' + lines + '</div><div class="card-footer">' + act + '</div></div>';
}

function viewDetail(taskId) {
  S.detailId = taskId;
  fetchTaskDetail(taskId).then(function() {
    navigateTo('page-detail');
    renderDetail(taskId);
  }).catch(function(err) { alert(err.message); });
}

function renderDetail(taskId) {
  var t = CACHE.tasks[taskId];
  if (!t) { document.getElementById('detail-content').innerHTML = '<div class="card"><div class="card-content">任务不存在</div></div>'; return; }
  var si = statusInfo(t.status, t), od = (t.status === 'borrowing' && isOverdue(t));
  var itemsH = ''; t.items.forEach(function(it) {
    itemsH += '<div style="display:flex;justify-content:space-between;padding:4px 0;border-bottom:1px solid #f5f5f5"><span>' + escapeHtml(it.sku) + ' ' + escapeHtml(it.size) + '</span><span>借' + it.qty + '件';
    if (it.picked > 0) itemsH += ' 提' + it.picked + '件';
    if (it.returned > 0) itemsH += ' 还' + it.returned + '件';
    itemsH += '</span></div>';
  });

  var info = '';
  if (t.type === 'express') {
    info += '<div><i class="fa-solid fa-truck"></i> 物流方式：快递</div>';
    if (t.logisticsNo) info += '<div style="color:#fa8c16">物流：' + escapeHtml(t.logisticsCompany || '') + ' ' + escapeHtml(t.logisticsNo) + '</div>';
    if (t.receiverName) info += '<div>收货人：' + escapeHtml(t.receiverName) + ' ' + escapeHtml(t.receiverPhone) + '</div>';
    if (t.receiverAddress) info += '<div>地址：' + escapeHtml(t.receiverAddress) + '</div>';
  } else {
    info += '<div><i class="fa-solid fa-store"></i> 物流方式：自提</div>';
    if (t.pickupStoreName) info += '<div>自提门店：' + escapeHtml(t.pickupStoreName) + '</div>';
    if (t.pickupStoreAddress) info += '<div>地址：' + escapeHtml(t.pickupStoreAddress) + '</div>';
  }
  if (t.dueDate) {
    if (od) info += '<div>预计归还：' + t.dueDate + ' <span class="red-text">超期' + overdueDays(t) + '天</span></div>';
    else info += '<div>预计归还：' + t.dueDate + ' 剩余' + remainDays(t) + '天</div>';
  }
  if (t.returnBatchNo) info += '<div style="color:#999;font-size:12px">归还批次：' + escapeHtml(t.returnBatchNo) + '</div>';
  if (t.returnLogisticsNo) info += '<div style="color:#13c2c2;font-size:12px">归还物流：' + escapeHtml(t.returnLogisticsNo) + '</div>';

  var tl = '<div class="timeline">';
  tl += '<div class="timeline-item"><div class="timeline-time">' + fmtDate(t.applyTime) + '</div><div class="timeline-content">借样申请提交成功</div></div>';
  if (t.borrowDate && t.type === 'pick_up') tl += '<div class="timeline-item"><div class="timeline-time">' + t.borrowDate + '</div><div class="timeline-content">到店自提</div></div>';
  if (t.borrowDate && t.type === 'express' && t.status !== 'pending') tl += '<div class="timeline-item"><div class="timeline-time">' + t.borrowDate + '</div><div class="timeline-content">门店已发货</div></div>';
  if (t.status === 'borrowing' || t.status === 'completed') tl += '<div class="timeline-item"><div class="timeline-time">' + (t.borrowDate||'-') + '</div><div class="timeline-content">确认收货，开始借样</div></div>';
  if (t.returnBatchNo) {
    tl += '<div class="timeline-item"><div class="timeline-time">-</div><div class="timeline-content">发起归还申请，批次号：' + escapeHtml(t.returnBatchNo) + '</div></div>';
    if (t.returnLogisticsNo) tl += '<div class="timeline-item"><div class="timeline-time">-</div><div class="timeline-content">物流信息已填写：' + escapeHtml(t.returnLogisticsNo) + '</div></div>';
  }
  if (t.status === 'store_pending') tl += '<div class="timeline-item"><div class="timeline-time">-</div><div class="timeline-content">等待门店收货并质检</div></div>';
  if (t.status === 'return_pending') tl += '<div class="timeline-item"><div class="timeline-time">-</div><div class="timeline-content">待填写物流单号</div></div>';
  if (t.status === 'return_logistics_filled') tl += '<div class="timeline-item"><div class="timeline-time">-</div><div class="timeline-content">物流运输中，等待仓库收货</div></div>';
  if (t.status === 'completed') tl += '<div class="timeline-item"><div class="timeline-time">-</div><div class="timeline-content">商品已销账，任务完结</div></div>';
  tl += '</div>';

  var act = '';
  if (t.status === 'pending') act = '<button class="btn btn-outline" onclick="simulateShip(\'' + taskId + '\')">模拟仓库发货</button>';
  else if (t.status === 'in_transit') act = '<button class="btn btn-primary" onclick="confirmReceive(\'' + taskId + '\')"><i class="fa-solid fa-check"></i> 确认收货</button>';
  else if (t.status === 'pending_pickup') act = '<button class="btn btn-primary" onclick="openPickupModal(\'' + taskId + '\')"><i class="fa-solid fa-clipboard-check"></i> 确认已自提</button>';
  else if (t.status === 'borrowing') act = '<button class="btn btn-primary" onclick="returnFromTask(\'' + taskId + '\')"><i class="fa-solid fa-rotate-left"></i> 发起归还</button>';
  else if (t.status === 'store_pending') act = '<button class="btn btn-success" onclick="simulateStoreReceive(\'' + taskId + '\')"><i class="fa-solid fa-store"></i> 模拟门店收货并质检通过</button>';
  else if (t.status === 'return_pending') act = '<button class="btn btn-primary" onclick="goToLogisticsForTask(\'' + taskId + '\')"><i class="fa-solid fa-box"></i> 填写物流单号</button>';
  else if (t.status === 'return_logistics_filled') act = '<div style="color:#13c2c2;font-size:13px;margin-bottom:8px">物流信息已填写，等待仓库收货</div><button class="btn btn-success" onclick="simulateExpressComplete(\'' + taskId + '\')"><i class="fa-solid fa-warehouse"></i> 模拟仓库收货完成</button>';

  document.getElementById('detail-content').innerHTML = '<div class="card"><div class="card-title"><span class="status-tag ' + si.c + '">' + si.t + si.i + '</span><span style="font-size:12px;color:#999">' + taskId + '</span></div><div class="card-content"><div style="margin-bottom:8px">' + info + '</div><div style="font-weight:bold;margin-bottom:8px">商品明细</div>' + itemsH + '<div style="margin-top:16px">' + act + '</div>' + tl + '</div></div>';
}

function initBorrow() {
  var u = getUser();
  document.getElementById('borrow-user-display').value = u.userName + ' / ' + u.userId;
  document.getElementById('borrow-shop-input').value = '';
  document.getElementById('borrow-shop-id').value = '';
  document.getElementById('borrow-shop-dropdown').style.display = 'none';
  document.getElementById('borrow-store-input').value = '';
  document.getElementById('borrow-store-id').value = '';
  document.getElementById('borrow-store-dropdown').style.display = 'none';
  document.getElementById('borrow-store-selected').style.display = 'none';
  document.getElementById('borrow-receiver').value = '';
  document.getElementById('borrow-phone').value = '';
  document.getElementById('borrow-address').value = '';
  document.getElementById('borrow-expected-date').value = '';
  document.querySelector('input[name="logistics-type"][value="express"]').checked = true;
  toggleBorrowLogistics();
  var tb = document.getElementById('sample-tbody'); tb.innerHTML = '';
  addSampleRow('SKU001', 'M', '1'); addSampleRow('SKU010', 'L', '1');
  renderBorrowShopDD();
}

function renderBorrowShopDD() {
  var u = getUser(), all = CACHE.shops, dd = document.getElementById('borrow-shop-dropdown'), h = '';
  all.forEach(function(s) {
    if (u.virtualShopIds.indexOf(s.virtualShopId) === -1) return;
    h += '<div class="shop-option" data-id="' + s.virtualShopId + '" onclick="selectBorrowShop(\'' + s.virtualShopId + '\',\'' + s.name.replace(/'/g, "\\'") + '\')">' + s.name + ' <span style="color:#999;font-size:11px">' + s.virtualShopId + '</span></div>';
  });
  dd.innerHTML = h || '<div style="padding:8px 12px;color:#999;font-size:13px">无可用虚店</div>';
  enableKeyboardActivation();
}
function filterBorrowShops() {
  var kw = document.getElementById('borrow-shop-input').value.toLowerCase();
  var opts = document.querySelectorAll('#borrow-shop-dropdown .shop-option'), vis = false;
  opts.forEach(function(o) {
    var t = o.textContent.toLowerCase(), id = (o.getAttribute('data-id') || '').toLowerCase();
    var m = !kw || t.indexOf(kw) >= 0 || id.indexOf(kw) >= 0;
    o.style.display = m ? '' : 'none'; if (m) vis = true;
  });
  document.getElementById('borrow-shop-dropdown').style.display = vis ? 'block' : 'none';
}
function selectBorrowShop(shopId, name) {
  document.getElementById('borrow-shop-input').value = name;
  document.getElementById('borrow-shop-id').value = shopId;
  document.getElementById('borrow-shop-dropdown').style.display = 'none';
  var shop = getShop(shopId), u = getUser();
  document.getElementById('borrow-receiver').value = u.userName || '';
  document.getElementById('borrow-phone').value = u.userId === 'lisi' ? '13900002222' : '13800001111';
  document.getElementById('borrow-address').value = shop && shop.address ? shop.address : '上海市闵行区申长路演示地址';
}
function toggleBorrowLogistics() {
  var isE = document.querySelector('input[name="logistics-type"]:checked').value === 'express';
  document.getElementById('express-fields').style.display = isE ? '' : 'none';
  document.getElementById('pickup-store-field').style.display = isE ? 'none' : '';
}
function filterBorrowStores() {
  var kw = document.getElementById('borrow-store-input').value.toLowerCase();
  var stores = CACHE.stores, dd = document.getElementById('borrow-store-dropdown'), h = '';
  stores.forEach(function(s) {
    if (s.status !== 'active') return;
    var nm = s.name.toLowerCase().indexOf(kw) >= 0, im = s.storeId.toLowerCase().indexOf(kw) >= 0;
    if (!kw || nm || im) h += '<div class="store-option" onclick="selectBorrowStore(\'' + s.storeId + '\',\'' + s.name.replace(/'/g, "\\'") + '\',\'' + s.address.replace(/'/g, "\\'") + '\')">' + s.name + ' <span class="sid">' + s.storeId + '</span></div>';
  });
  dd.innerHTML = h || '<div style="padding:8px 12px;color:#999;font-size:13px">未找到匹配门店</div>';
  dd.style.display = 'block';
  enableKeyboardActivation();
}
function selectBorrowStore(id, name, addr) {
  document.getElementById('borrow-store-input').value = name + ' (' + id + ')';
  document.getElementById('borrow-store-id').value = id;
  document.getElementById('borrow-store-dropdown').style.display = 'none';
  var sel = document.getElementById('borrow-store-selected');
  sel.innerHTML = '<span class="store-selected-tag"><i class="fa-solid fa-check"></i> ' + name + ' (' + id + ')</span>';
  sel.style.display = 'block';
}
function addSampleRow(sku, size, qty) {
  var tb = document.getElementById('sample-tbody'), tr = document.createElement('tr');
  tr.innerHTML = '<td><input type="text" class="sample-sku" value="' + (sku||'') + '" placeholder="编码"></td>' +
    '<td><input type="text" class="sample-size" value="' + (size||'') + '" placeholder="尺码"></td>' +
    '<td><input type="number" class="sample-qty" value="' + (qty||'') + '" placeholder="数量" min="1" style="width:100%"></td>' +
    '<td><button class="del-row-btn" type="button" onclick="this.closest(\'tr\').remove();updateSampleSummary()">删除</button></td>';
  tb.appendChild(tr);
  tr.querySelectorAll('input').forEach(function(inp) { inp.addEventListener('input', updateSampleSummary); });
  updateSampleSummary();
}
function updateSampleSummary() {
  var rows = document.querySelectorAll('#sample-tbody tr'), total = 0;
  rows.forEach(function(r) { total += parseInt(r.querySelector('.sample-qty').value) || 0; });
  document.getElementById('sample-summary').textContent = '共' + total + '件商品';
}
function getSampleItems() {
  var rows = document.querySelectorAll('#sample-tbody tr'), items = [];
  rows.forEach(function(r) {
    var sku = r.querySelector('.sample-sku').value.trim(), size = r.querySelector('.sample-size').value.trim(), qty = parseInt(r.querySelector('.sample-qty').value) || 0;
    if (sku && size && qty > 0) items.push({ sku: sku, size: size, qty: qty, picked: 0, returned: 0 });
  });
  return items;
}

function submitBorrow() {
  var shopId = document.getElementById('borrow-shop-id').value;
  if (!shopId) { alert('请选择借入虚店'); return; }
  var logType = document.querySelector('input[name="logistics-type"]:checked').value;
  var items = getSampleItems();
  if (!items.length) { alert('请添加样品'); return; }
  var body = {
    virtualStoreCode: shopId,
    deliveryType: logType === 'pick_up' ? 'PICKUP' : 'EXPRESS',
    pickupStoreCode: logType === 'pick_up' ? document.getElementById('borrow-store-id').value : null,
    remark: 'H5 V1.5 借样申请',
    receiver: logType === 'express' ? {
      name: document.getElementById('borrow-receiver').value.trim(),
      mobile: document.getElementById('borrow-phone').value.trim(),
      province: '上海市',
      city: '上海市',
      district: '闵行区',
      address: document.getElementById('borrow-address').value.trim()
    } : null,
    items: items.map(function(it) { return {skuCode: it.sku, sizeCode: it.size, applyQty: it.qty}; })
  };
  if (logType === 'pick_up' && !body.pickupStoreCode) { alert('请选择自提门店'); return; }
  apiPost('/api/v1/borrow/applications', body).then(function(res) {
    alert('借样申请已提交：' + res.applyNo);
    return refreshTasks().then(function() {
      if (res.taskNos && res.taskNos[0]) viewDetail(res.taskNos[0]);
      else navigateTo('page-main');
    });
  }).catch(function(err) { alert(err.message); });
}

function simulateShip(taskId) {
  apiPost('/api/v1/borrow/tasks/' + encodeURIComponent(taskId) + '/ship-confirm', {
    logisticsCompany: '顺丰速运',
    logisticsNo: 'SF' + String(Math.floor(Math.random() * 9e9) + 1e9)
  }).then(function(res) {
    alert(res.message || '发货完成');
    return refreshTasks();
  }).then(function() {
    if (document.getElementById('page-detail').classList.contains('active')) viewDetail(taskId);
  }).catch(function(err) { alert(err.message); });
}

function confirmReceive(taskId) {
  var t = CACHE.tasks[taskId]; if (!t) return;
  apiPost('/api/v1/borrow/tasks/' + encodeURIComponent(taskId) + '/receive-confirm', {
    logisticsNo: t.logisticsNo || ('SF' + Date.now()),
    remark: 'H5 确认收货'
  }).then(function(res) {
    alert((res.message || '已确认收货') + '\n状态更新为"借样中"');
    return refreshTasks();
  }).then(function() {
    if (document.getElementById('page-detail').classList.contains('active')) viewDetail(taskId);
  }).catch(function(err) { alert(err.message); });
}

function openPickupModal(taskId) {
  var proceed = function() {
    var t = CACHE.tasks[taskId]; if (!t || t.status !== 'pending_pickup') return;
    S.detailId = taskId;
    var h = '';
    t.items.forEach(function(it, idx) {
      var rem = Math.max(0, it.qty - it.picked);
      h += '<div style="display:flex;align-items:center;gap:8px;padding:8px 0;border-bottom:1px solid #f0f0f0;flex-wrap:wrap">' +
        '<input type="checkbox" class="pk-cb" data-idx="' + idx + '" checked>' +
        '<span style="flex:1;min-width:80px">' + escapeHtml(it.sku) + ' ' + escapeHtml(it.size) + '</span>' +
        '<span style="color:#999;font-size:12px">应提' + it.qty + ' 待提' + rem + '</span>' +
        '<input type="number" class="pk-qty" data-idx="' + idx + '" value="' + rem + '" min="1" max="' + rem + '" style="width:52px;text-align:center;padding:4px;border:1px solid #d9d9d9;border-radius:4px">' +
        '<span style="font-size:12px">件</span></div>';
    });
    document.getElementById('pickup-modal-items').innerHTML = h;
    openModal('pickup-modal');
  };
  if (CACHE.details[taskId]) {
    proceed();
    return;
  }
  fetchTaskDetail(taskId).then(proceed).catch(function(err) { alert(err.message); });
}

function confirmPickupSubmit() {
  var t = CACHE.tasks[S.detailId]; if (!t) return;
  var items = [];
  document.querySelectorAll('.pk-cb').forEach(function(cb) {
    if (cb.checked) {
      var idx = parseInt(cb.getAttribute('data-idx'));
      var qi = document.querySelector('.pk-qty[data-idx="' + idx + '"]');
      var q = parseInt(qi && qi.value) || 0;
      if (q > 0 && t.items[idx]) items.push({taskItemId: t.items[idx].taskItemId, confirmQty: q});
    }
  });
  if (!items.length) { alert('请至少勾选一件商品'); return; }
  apiPost('/api/v1/borrow/tasks/' + encodeURIComponent(S.detailId) + '/pickup-confirm', {
    items: items,
    remark: 'H5 确认自提'
  }).then(function(res) {
    closeModal('pickup-modal');
    alert(res.message || '自提确认完成');
    return refreshTasks();
  }).then(function() {
    if (document.getElementById('page-detail').classList.contains('active')) viewDetail(S.detailId);
  }).catch(function(err) { alert(err.message); });
}

function initReturn() {
  document.getElementById('return-shop-input').value = '';
  document.getElementById('return-shop-id').value = '';
  document.getElementById('return-shop-dropdown').style.display = 'none';
  document.getElementById('return-items-card').style.display = 'none';
  document.getElementById('return-method-box').style.display = 'none';
  document.getElementById('return-store-card').style.display = 'none';
  document.getElementById('return-summary-bar').style.display = 'none';
  document.getElementById('return-submit-area').style.display = 'none';
  document.getElementById('return-sku-filter').value = '';
  document.getElementById('return-store-input').value = '';
  document.getElementById('return-store-id').value = '';
  document.getElementById('return-store-dropdown').style.display = 'none';
  document.getElementById('return-store-selected').style.display = 'none';
  S.retShop = ''; S.retMethod = 'express'; S.retItems = []; S.retStoreId = ''; S.retStoreName = '';
  refreshRetMethod(); renderRetShopDD();
}
function renderRetShopDD() {
  var u = getUser(), all = CACHE.shops, dd = document.getElementById('return-shop-dropdown'), h = '';
  all.forEach(function(s) {
    if (u.virtualShopIds.indexOf(s.virtualShopId) === -1) return;
    h += '<div class="shop-option" data-id="' + s.virtualShopId + '" onclick="selectRetShop(\'' + s.virtualShopId + '\',\'' + s.name.replace(/'/g, "\\'") + '\')">' + s.name + ' <span style="color:#999;font-size:11px">' + s.virtualShopId + '</span></div>';
  });
  dd.innerHTML = h || '<div style="padding:8px 12px;color:#999;font-size:13px">无可用虚店</div>';
  enableKeyboardActivation();
}
function filterReturnShops() {
  var kw = document.getElementById('return-shop-input').value.toLowerCase();
  var opts = document.querySelectorAll('#return-shop-dropdown .shop-option'), vis = false;
  opts.forEach(function(o) {
    var t = o.textContent.toLowerCase(), id = (o.getAttribute('data-id') || '').toLowerCase();
    var m = !kw || t.indexOf(kw) >= 0 || id.indexOf(kw) >= 0;
    o.style.display = m ? '' : 'none'; if (m) vis = true;
  });
  document.getElementById('return-shop-dropdown').style.display = vis ? 'block' : 'none';
}
function selectRetShop(id, name) {
  document.getElementById('return-shop-input').value = name;
  document.getElementById('return-shop-id').value = id;
  document.getElementById('return-shop-dropdown').style.display = 'none';
  S.retShop = id; S.retMethod = 'express'; S.retStoreId = ''; S.retStoreName = '';
  refreshRetMethod(); aggregateRetItems();
}
function aggregateRetItems() {
  var shopId = S.retShop; if (!shopId) return;
  apiGet('/api/v1/returns/aggregations?virtualStoreCode=' + encodeURIComponent(shopId) + '&sampleFilterType=ALL').then(function(data) {
    S.retItems = (data.rows || []).map(function(row) {
      return {
        sku: row.skuCode,
        size: row.sizeCode,
        totalQty: row.availableReturnQty,
        storeName: row.sourceStoreName || '',
        isPickup: row.sampleType === 'PICKUP',
        refs: (row.taskRefs || []).map(function(ref) {
          return {taskId: ref.taskNo, available: ref.remainingQty, borrowDate: ref.borrowedAt};
        })
      };
    });
    renderRetItems();
  }).catch(function(err) { alert(err.message); });
}
function renderRetItems() {
  var items = S.retItems, card = document.getElementById('return-items-card'), c = document.getElementById('return-items-list');
  if (!items.length) {
    card.style.display = 'block';
    c.innerHTML = '<div style="text-align:center;color:#999;padding:20px">该虚店下暂无可还商品</div>';
    document.getElementById('return-method-box').style.display = 'none';
    document.getElementById('return-summary-bar').style.display = 'none';
    document.getElementById('return-submit-area').style.display = 'none';
    return;
  }
  card.style.display = 'block';
  document.getElementById('return-method-box').style.display = 'block';
  document.getElementById('return-summary-bar').style.display = 'flex';
  document.getElementById('return-submit-area').style.display = 'block';
  refreshRetMethod();
  var h = '';
  items.forEach(function(it, idx) {
    var st = (it.isPickup && it.storeName) ? '<div class="return-item-store">来源门店：' + escapeHtml(it.storeName) + '</div>' : '';
    h += '<div class="return-item-row">' +
      '<input type="checkbox" class="ret-cb" data-idx="' + idx + '" onchange="updateRetSummary()">' +
      '<div class="return-item-info"><div class="return-item-sku">' + escapeHtml(it.sku) + ' ' + escapeHtml(it.size) + '</div>' + st + '</div>' +
      '<div class="return-item-qty">可还 ' + it.totalQty + ' 件</div>' +
      '<input type="number" class="return-qty-input ret-qty" data-idx="' + idx + '" value="' + it.totalQty + '" min="0" max="' + it.totalQty + '" onfocus="autoCheckRet(this)" oninput="autoCheckRet(this);updateRetSummary()">' +
      '</div>';
  });
  c.innerHTML = h;
  updateRetSummary();
}
function autoCheckRet(inp) {
  var idx = inp.getAttribute('data-idx'), cb = document.querySelector('.ret-cb[data-idx="' + idx + '"]');
  if (cb && !cb.checked && (parseInt(inp.value) || 0) > 0) cb.checked = true;
}
function filterReturnItems() {
  var kw = document.getElementById('return-sku-filter').value.toLowerCase();
  document.querySelectorAll('#return-items-list .return-item-row').forEach(function(r) {
    var s = r.querySelector('.return-item-sku');
    if (s) r.style.display = (!kw || s.textContent.toLowerCase().indexOf(kw) >= 0) ? '' : 'none';
  });
}
function switchReturnMethod(m) {
  if (S.retMethod === m) return;
  S.retMethod = m;
  document.querySelectorAll('.ret-cb').forEach(function(cb) { cb.checked = false; });
  document.querySelectorAll('.ret-qty').forEach(function(i) { i.value = i.max; });
  S.retStoreId = ''; S.retStoreName = '';
  document.getElementById('return-store-input').value = '';
  document.getElementById('return-store-id').value = '';
  document.getElementById('return-store-selected').style.display = 'none';
  updateRetSummary(); refreshRetMethod();
  alert('归还方式已切换，已清空商品选择，请重新勾选。');
}
function refreshRetMethod() {
  var has = !!S.retShop;
  document.getElementById('return-method-box').style.display = has ? 'block' : 'none';
  document.getElementById('return-store-card').style.display = (has && S.retMethod === 'in_person') ? 'block' : 'none';
  document.getElementById('method-express').classList.toggle('selected', S.retMethod === 'express');
  document.getElementById('method-inperson').classList.toggle('selected', S.retMethod === 'in_person');
}
function updateRetSummary() {
  var skuC = 0, qtyC = 0;
  document.querySelectorAll('.ret-cb').forEach(function(cb) {
    if (cb.checked) {
      var idx = parseInt(cb.getAttribute('data-idx'));
      var qi = document.querySelector('.ret-qty[data-idx="' + idx + '"]');
      if (qi) {
        var q = parseInt(qi.value) || 0, it = S.retItems[idx];
        if (it && q > it.totalQty) q = it.totalQty;
        if (q > 0) { skuC++; qtyC += q; }
      }
    }
  });
  document.getElementById('summary-sku-count').textContent = skuC;
  document.getElementById('summary-qty-count').textContent = qtyC;
  document.getElementById('return-submit-btn').textContent = '确认还样（' + qtyC + '件）';
}
function filterReturnStores() {
  var kw = document.getElementById('return-store-input').value.toLowerCase();
  var stores = CACHE.stores, dd = document.getElementById('return-store-dropdown'), h = '';
  stores.forEach(function(s) {
    var nm = s.name.toLowerCase().indexOf(kw) >= 0, im = s.storeId.toLowerCase().indexOf(kw) >= 0;
    if (!kw || nm || im) {
      var tag = s.status !== 'active' ? ' <span style="color:#f5222d;font-size:11px">(已关店)</span>' : '';
      h += '<div class="store-option" data-id="' + s.storeId + '" data-name="' + s.name + '" data-active="' + (s.status === 'active') + '" onclick="selectRetStore(this)">' + s.name + ' <span class="sid">' + s.storeId + '</span>' + tag + '</div>';
    }
  });
  dd.innerHTML = h || '<div style="padding:8px 12px;color:#999;font-size:13px">未找到匹配门店</div>';
  dd.style.display = 'block';
  enableKeyboardActivation();
}
function selectRetStore(el) {
  var id = el.getAttribute('data-id'), name = el.getAttribute('data-name'), active = el.getAttribute('data-active') === 'true';
  if (!active) { alert('该门店（' + name + '）已关店，无法选择'); return; }
  document.getElementById('return-store-input').value = name + ' (' + id + ')';
  document.getElementById('return-store-id').value = id;
  document.getElementById('return-store-dropdown').style.display = 'none';
  S.retStoreId = id; S.retStoreName = name;
  var sel = document.getElementById('return-store-selected');
  sel.innerHTML = '<span class="store-selected-tag"><i class="fa-solid fa-check"></i> ' + name + ' (' + id + ')</span>';
  sel.style.display = 'block';
}
function submitReturn() {
  var sels = [], has = false;
  document.querySelectorAll('.ret-cb').forEach(function(cb) {
    if (cb.checked) {
      var idx = parseInt(cb.getAttribute('data-idx'));
      var qi = document.querySelector('.ret-qty[data-idx="' + idx + '"]');
      if (qi) {
        var q = parseInt(qi.value) || 0, it = S.retItems[idx];
        if (it && q > it.totalQty) q = it.totalQty;
        if (q > 0) { sels.push({ sku: it.sku, size: it.size, qty: q, refs: it.refs }); has = true; }
      }
    }
  });
  if (!has) { alert('请至少选择一件商品并填写归还数量'); return; }
  if (S.retMethod === 'in_person' && !S.retStoreId) { alert('自提归还必须选择归还门店'); return; }
  var body = {
    virtualStoreCode: S.retShop,
    sourceType: 'GENERAL_RETURN',
    sampleFilterType: 'ALL',
    returnMethod: S.retMethod === 'in_person' ? 'IN_PERSON' : 'EXPRESS',
    remark: S.retMethod === 'in_person' ? ('归还门店：' + S.retStoreName) : 'H5 通用还样',
    items: sels.map(function(sel) { return {skuCode: sel.sku, sizeCode: sel.size, applyReturnQty: sel.qty}; })
  };
  apiPost('/api/v1/returns/batches', body).then(function(res) {
    S.batchNo = res.returnBatchNo;
    S.allocMethod = S.retMethod;
    S.allocs = [];
    (res.taskSummaries || []).forEach(function(item) {
      S.allocs.push({taskId: item.taskNo, sku: '', size: '', qty: item.allocatedQty});
    });
    renderRetResult(res.returnBatchNo, S.allocs, S.retMethod, S.retStoreName);
    S.stack.push('page-return-result');
    document.querySelectorAll('.page').forEach(function(p) { p.classList.remove('active'); });
    document.getElementById('page-return-result').classList.add('active');
    window.scrollTo(0, 0);
    return refreshTasks();
  }).catch(function(err) { alert(err.message); });
}
function renderRetResult(batchNo, allocs, method, stName) {
  document.getElementById('result-batch-no').textContent = batchNo;
  var h = '';
  allocs.forEach(function(a) {
    var mt = method === 'in_person' ? '自提归还' : '快递归还';
    var sl = (method === 'in_person' && stName) ? '<div style="font-size:11px;color:#722ed1">归还至：' + escapeHtml(stName) + '</div>' : '';
    h += '<div class="batch-task-item"><div style="font-weight:500">' + escapeHtml(a.taskId) + '</div><div>' + escapeHtml(a.sku || '-') + ' ' + escapeHtml(a.size || '') + ' x ' + a.qty + '件 · ' + mt + '</div>' + sl + '</div>';
  });
  document.getElementById('result-task-list').innerHTML = h;
  var ae = document.getElementById('result-actions');
  if (method === 'express') {
    ae.innerHTML = '<button class="btn btn-primary" style="width:100%;padding:12px;font-size:15px;font-weight:bold" onclick="goToLogistics()"><i class="fa-solid fa-box"></i> 填写物流单号</button>' +
      '<button class="btn btn-link" style="width:100%;margin-top:8px" onclick="navigateTo(\'page-main\')">返回任务列表</button>';
  } else {
    ae.innerHTML = '<button class="btn btn-success" style="width:100%;padding:12px;font-size:15px;font-weight:bold" onclick="navigateTo(\'page-main\')"><i class="fa-solid fa-clipboard-list"></i> 查看任务列表</button>' +
      '<button class="btn btn-link" style="width:100%;margin-top:8px" onclick="navigateTo(\'page-main\')">返回任务列表</button>';
  }
}
function goToLogistics() {
  document.getElementById('logistics-batch-no').textContent = S.batchNo;
  var cnt = S.allocs.length, tq = S.allocs.reduce(function(s, a) { return s + a.qty; }, 0);
  document.getElementById('logistics-batch-desc').textContent = '共关联 ' + cnt + ' 个任务，合计 ' + tq + ' 件商品';
  document.getElementById('logistics-no').value = '';
  navigateTo('page-logistics');
}
function submitLogistics() {
  var ln = document.getElementById('logistics-no').value.trim();
  if (!ln) { alert('请输入快递单号'); return; }
  var co = document.getElementById('logistics-company').value, bn = S.batchNo;
  apiPost('/api/v1/returns/batches/' + encodeURIComponent(bn) + '/logistics', {
    companyCode: companyCode(co),
    companyName: co,
    logisticsNo: ln
  }).then(function() {
    alert('物流信息提交成功！\n批次号：' + bn + '\n快递：' + co + ' ' + ln + '\n已回填到所有关联任务');
    return refreshTasks();
  }).then(function() { navigateTo('page-main'); }).catch(function(err) { alert(err.message); });
}
function companyCode(name) {
  if (name.indexOf('顺丰') >= 0) return 'SF';
  if (name.indexOf('中通') >= 0) return 'ZTO';
  if (name.indexOf('圆通') >= 0) return 'YTO';
  if (name.indexOf('韵达') >= 0) return 'YD';
  if (name.indexOf('申通') >= 0) return 'STO';
  return 'EXP';
}
function returnFromTask(taskId) {
  var t = CACHE.tasks[taskId]; if (!t) return;
  navigateTo('page-return');
  setTimeout(function() {
    var sid = t.virtualShopId, shop = getShop(sid);
    if (sid && shop) selectRetShop(sid, shop.name);
  }, 50);
}
function simulateStoreReceive(taskId) {
  completeTaskReturn(taskId, '门店已确认收货并质检通过，任务已完结');
}
function simulateExpressComplete(taskId) {
  completeTaskReturn(taskId, '仓库已确认收货并质检通过，任务已完结');
}
function completeTaskReturn(taskId, message) {
  var t = CACHE.tasks[taskId];
  var completion = t && t.returnBatchNo
    ? apiPost('/api/v1/returns/batches/' + encodeURIComponent(t.returnBatchNo) + '/complete')
    : apiPost('/api/v1/borrow/tasks/' + encodeURIComponent(taskId) + '/return-complete');
  completion.then(function() {
    alert(message);
    return refreshTasks();
  }).then(function() {
    if (document.getElementById('page-detail').classList.contains('active')) viewDetail(taskId);
  }).catch(function(err) { alert(err.message); });
}
function goToLogisticsForTask(taskId) {
  var proceed = function(t) {
    if (!t || !t.returnBatchNo) { alert('该任务没有归还批次'); return; }
    S.batchNo = t.returnBatchNo;
    S.allocs = [{ taskId: taskId, sku: t.items[0] ? t.items[0].sku : '', size: t.items[0] ? t.items[0].size : '', qty: t.items[0] ? t.items[0].qty : 0 }];
    S.allocMethod = 'express';
    goToLogistics();
  };
  var t = CACHE.tasks[taskId];
  if (t && t.returnBatchNo) {
    proceed(t);
    return;
  }
  fetchTaskDetail(taskId).then(proceed).catch(function(err) { alert(err.message); });
}
function doSkuSearch() {
  var sku = document.getElementById('sku-search-input').value.trim();
  if (!sku) { alert('请输入商品编码'); return; }
  S.searching = true;
  document.getElementById('search-clear-btn').style.display = 'block';
  document.getElementById('main-tab-bar').style.display = 'none';
  document.getElementById('content-pending').style.display = 'none';
  document.getElementById('content-borrowing').style.display = 'none';
  document.getElementById('content-history').style.display = 'none';
  document.getElementById('search-results-area').style.display = 'block';
  var results = [], tasks = userTasks(), su = sku.toUpperCase();
  for (var id in tasks) {
    var matched = tasks[id].items.filter(function(it) { return it.sku && it.sku.toUpperCase() === su; });
    if (matched.length > 0) results.push({ taskId: id, task: tasks[id], matched: matched });
  }
  renderSearchResults(results, sku);
}
function renderSearchResults(results, sku) {
  var te = document.getElementById('search-results-title'), c = document.getElementById('search-results-cards');
  if (!results.length) {
    te.textContent = '未找到包含 "' + sku + '" 的任务';
    c.innerHTML = '<div class="empty-state"><div class="empty-icon"><i class="fa-solid fa-magnifying-glass"></i></div>未找到包含该商品编码的任务</div>';
    return;
  }
  te.textContent = '找到 ' + results.length + ' 个包含 "' + sku + '" 的任务';
  var h = '';
  results.forEach(function(r) {
    var t = r.task, si = statusInfo(t.status, t);
    var mh = ''; r.matched.forEach(function(it) { mh += '<div><span class="search-result-match">' + escapeHtml(it.sku) + '</span> ' + escapeHtml(it.size) + ' x' + it.qty + '</div>'; });
    h += '<div class="card search-result-card" onclick="viewDetail(\'' + r.taskId + '\')"><div class="card-title"><span class="status-tag ' + si.c + '">' + si.t + si.i + '</span><span style="font-size:12px;color:#999">' + r.taskId + '</span></div><div class="card-content"><div style="font-weight:bold;margin-bottom:4px;color:#1677FF">匹配商品：</div>' + mh + '</div></div>';
  });
  c.innerHTML = h;
}
function clearSearch() {
  S.searching = false;
  document.getElementById('sku-search-input').value = '';
  document.getElementById('search-clear-btn').style.display = 'none';
  document.getElementById('search-results-area').style.display = 'none';
  document.getElementById('main-tab-bar').style.display = 'flex';
  document.getElementById('content-pending').style.display = '';
  document.getElementById('content-borrowing').style.display = '';
  document.getElementById('content-history').style.display = '';
  switchMainTab(S.tab);
}
function showImportModal() {
  document.getElementById('import-paste-text').value = '';
  document.getElementById('import-error-area').style.display = 'none';
  switchImportTab('text'); openModal('import-modal');
}
function switchImportTab(tab) {
  document.getElementById('import-tab-text').classList.toggle('active', tab === 'text');
  document.getElementById('import-tab-file').classList.toggle('active', tab === 'file');
  document.getElementById('import-text-area').style.display = tab === 'text' ? '' : 'none';
  document.getElementById('import-file-area').style.display = tab === 'file' ? '' : 'none';
}
function simulateFileUpload() {
  document.getElementById('import-paste-text').value = 'SKU001 M 1\nSKU010 L 1';
  switchImportTab('text'); alert('已模拟上传Excel文件');
}
function executeImport() {
  var text = document.getElementById('import-paste-text').value.trim();
  if (!text) { alert('请上传Excel或粘贴文本数据'); return; }
  var lines = text.split('\n').filter(function(l) { return l.trim(); });
  var imports = [], errors = [];
  lines.forEach(function(line, i) {
    var p = line.trim().split(/\s+/);
    if (p.length < 3) { errors.push('第' + (i+1) + '行格式错误：' + line); return; }
    var qty = parseInt(p[2]);
    if (!p[0] || !p[1] || !qty || qty <= 0) { errors.push('第' + (i+1) + '行数据无效：' + line); return; }
    imports.push({ sku: p[0], size: p[1], qty: qty });
  });
  var matched = [], unmatched = [];
  imports.forEach(function(imp) {
    var found = S.retItems.find(function(a) { return a.sku.toUpperCase() === imp.sku.toUpperCase() && a.size === imp.size; });
    if (found) matched.push({ item: found, qty: imp.qty }); else unmatched.push(imp.sku + ' ' + imp.size);
  });
  var ea = document.getElementById('import-error-area');
  if (unmatched.length || errors.length) {
    var eh = ''; errors.forEach(function(e) { eh += '<div>' + e + '</div>'; });
    unmatched.forEach(function(s) { eh += '<div>匹配失败：' + s + '</div>'; });
    ea.innerHTML = eh; ea.style.display = 'block';
  } else ea.style.display = 'none';
  matched.forEach(function(m) {
    var idx = S.retItems.indexOf(m.item);
    var cb = document.querySelector('.ret-cb[data-idx="' + idx + '"]');
    var qi = document.querySelector('.ret-qty[data-idx="' + idx + '"]');
    if (cb && qi) { cb.checked = true; qi.value = Math.min(m.qty, m.item.totalQty); }
  });
  updateRetSummary();
  if (matched.length) {
    setTimeout(function() {
      closeModal('import-modal');
      alert('导入成功！已匹配 ' + matched.length + ' 种' + (unmatched.length ? '，' + unmatched.length + ' 种失败' : ''));
    }, 300);
  }
}
function openModal(id) { document.getElementById(id).classList.add('active'); }
function closeModal(id) { document.getElementById(id).classList.remove('active'); }
function enableKeyboardActivation() {
  document.querySelectorAll('.tab-item,.filter-pill,.method-option,.shop-option,.store-option').forEach(function(el) {
    if (!el.hasAttribute('tabindex')) el.setAttribute('tabindex', '0');
    if (!el.hasAttribute('role')) el.setAttribute('role', 'button');
  });
}
document.addEventListener('click', function(e) {
  if (!e.target.closest('.search-select-wrapper')) document.querySelectorAll('.shop-dropdown').forEach(function(d) { d.style.display = 'none'; });
  if (!e.target.closest('.store-search-wrapper')) document.querySelectorAll('.store-dropdown').forEach(function(d) { d.style.display = 'none'; });
});
document.addEventListener('keydown', function(e) {
  if ((e.key === 'Enter' || e.key === ' ') && e.target && e.target.getAttribute('role') === 'button') {
    e.preventDefault();
    e.target.click();
  }
  if (e.key === 'Escape') {
    document.querySelectorAll('.modal-mask.active').forEach(function(modal) { modal.classList.remove('active'); });
    document.querySelectorAll('.shop-dropdown,.store-dropdown').forEach(function(d) { d.style.display = 'none'; });
  }
});
function resetAllData() {
  alert('当前版本已接入 MySQL，不支持从页面清空服务端数据。需要重置请执行数据库 seed 脚本。');
}
window.onload = function() {
  enableKeyboardActivation();
  apiGet('/api/v1/common/demo-metadata').then(function(meta) {
    CACHE.users = meta.users || [];
    CACHE.shops = (meta.virtualShops || []).map(function(s) {
      return {
        virtualShopId: s.virtualStoreCode,
        name: s.virtualStoreName,
        address: '上海市闵行区申长路演示地址',
        contact: '演示用户 13800000000'
      };
    });
    CACHE.stores = meta.stores || [];
    initUserSelector();
    return refreshTasks();
  }).catch(function(err) {
    document.getElementById('pending-cards').innerHTML = '<div class="empty-state">' + escapeHtml(err.message) + '</div>';
  }).finally(function() {
    enableKeyboardActivation();
  });
};
