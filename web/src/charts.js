import * as d3 from 'd3'

const SPARK = ['#e85d04', '#f48c06', '#dc2f02', '#9a3412', '#fb923c', '#7c2d12', '#fdba74', '#c2410c']

export function paint(fn) {
  requestAnimationFrame(() => requestAnimationFrame(() => {
    try { fn() } catch (err) { console.error('chart', err) }
  }))
}

function clear(el) {
  d3.select(el).selectAll('*').remove()
}

function width(el, fallback = 640) {
  return Math.max(220, (el && el.clientWidth) || fallback)
}

let tip
function ensureTip() {
  if (!tip) {
    tip = d3.select('body').append('div').attr('class', 'spark-tip')
  }
  return tip
}

export function showTip(html, ev) {
  const node = ensureTip()
  node.html(html).style('display', 'block')
  moveTip(ev)
}

export function moveTip(ev) {
  if (!tip) return
  const pad = 14
  const tw = tip.node().offsetWidth || 160
  const th = tip.node().offsetHeight || 40
  let x = ev.clientX + pad
  let y = ev.clientY + pad
  if (x + tw > window.innerWidth - 8) x = ev.clientX - tw - 8
  if (y + th > window.innerHeight - 8) y = ev.clientY - th - 8
  tip.style('left', x + 'px').style('top', y + 'px')
}

export function hideTip() {
  if (tip) tip.style('display', 'none')
}

function bindTip(sel, htmlFn) {
  sel
    .on('pointerenter', (ev, d) => showTip(htmlFn(d), ev))
    .on('pointermove', ev => moveTip(ev))
    .on('pointerleave', hideTip)
}

function bindClick(sel, fn) {
  if (!fn) return
  sel.style('cursor', 'pointer').on('click', (ev, d) => {
    hideTip()
    fn(d)
  })
}

function rowsOf(items) {
  return (items || []).filter(d => d && d.count > 0)
}

export function donut(el, items, { title = '', onClick } = {}) {
  if (!el) return
  clear(el)
  const data = rowsOf(items)
  if (!data.length) return
  const total = d3.sum(data, d => d.count) || 1
  const w = width(el)
  const h = Math.min(280, Math.max(200, w * 0.55))
  const r = Math.min(w, h) / 2 - 10
  const svg = d3.select(el).append('svg').attr('width', w).attr('height', h)
  const g = svg.append('g').attr('transform', `translate(${w / 2},${h / 2})`)
  const pie = d3.pie().value(d => d.count).sort(null)
  const arc = d3.arc().innerRadius(r * 0.52).outerRadius(r)
  const color = d3.scaleOrdinal(SPARK)
  const slices = g.selectAll('path').data(pie(data)).enter().append('path')
    .attr('d', arc)
    .attr('fill', d => color(d.data.value))
    .attr('stroke', '#fffdf9')
    .attr('stroke-width', 1.5)
  bindTip(slices, d => {
    const row = d.data
    const pct = ((100 * row.count) / total).toFixed(1)
    return `<strong>${row.value}</strong><br>${row.count} · ${pct}% of ${total}<br><em>click to filter</em>`
  })
  bindClick(slices, d => onClick && onClick(d.data))
  g.append('text').attr('text-anchor', 'middle').attr('dy', '0.35em')
    .style('font-size', '0.85rem').style('fill', '#78716c').text(title || `${total}`)
}

export function areaChart(el, series) {
  if (!el) return
  clear(el)
  const data = (series || []).map((d, i) => ({ ...d, i }))
  if (!data.length) return
  const w = width(el, 720)
  const h = 180
  const m = { t: 12, r: 16, b: 36, l: 36 }
  const svg = d3.select(el).append('svg').attr('width', w).attr('height', h)
  const x = d3.scalePoint().domain(data.map(d => d.t)).range([m.l, w - m.r])
  const y = d3.scaleLinear().domain([0, d3.max(data, d => d.n) || 1]).nice().range([h - m.b, m.t])
  const line = d3.line().defined(d => x(d.t) != null).x(d => x(d.t)).y(d => y(d.n)).curve(d3.curveMonotoneX)
  const area = d3.area().defined(d => x(d.t) != null).x(d => x(d.t)).y0(h - m.b).y1(d => y(d.n)).curve(d3.curveMonotoneX)
  if (data.length > 1) {
    svg.append('path').datum(data).attr('d', area).attr('fill', '#fed7aa').attr('opacity', 0.9)
    svg.append('path').datum(data).attr('d', line).attr('fill', 'none').attr('stroke', '#e85d04').attr('stroke-width', 2)
  }
  const ticks = data.filter((_, i) => i % Math.ceil(data.length / 7) === 0)
  svg.append('g').attr('transform', `translate(0,${h - m.b})`)
    .call(d3.axisBottom(x).tickValues(ticks.map(d => d.t)))
    .selectAll('text').style('font-size', '10px').attr('transform', 'rotate(-30)').style('text-anchor', 'end')
  svg.append('g').attr('transform', `translate(${m.l},0)`).call(d3.axisLeft(y).ticks(4))
    .selectAll('text').style('font-size', '10px')
  const dots = svg.selectAll('circle.pt').data(data).enter().append('circle')
    .attr('class', 'pt')
    .attr('cx', d => x(d.t)).attr('cy', d => y(d.n)).attr('r', 3.5).attr('fill', '#c2410c')
    .style('pointer-events', 'none')
  const focus = svg.append('g').style('display', 'none').style('pointer-events', 'none')
  focus.append('line')
    .attr('class', 'rule')
    .attr('y1', m.t).attr('y2', h - m.b)
    .attr('stroke', '#9a3412').attr('stroke-dasharray', '3,2').attr('opacity', 0.7)
  const focusDot = focus.append('circle').attr('r', 6).attr('fill', '#9a3412').attr('stroke', '#fffdf9').attr('stroke-width', 1.5)

  function htmlFor(d) {
    const n = d.n || 0
    return `<strong>${d.t}</strong><br>${n} page${n === 1 ? '' : 's'} fetched`
  }
  function nearest(ev) {
    const [mx] = d3.pointer(ev, svg.node())
    let best = data[0]
    let bestD = Infinity
    for (const d of data) {
      const px = x(d.t)
      if (px == null) continue
      const dx = Math.abs(px - mx)
      if (dx < bestD) {
        bestD = dx
        best = d
      }
    }
    return best
  }
  function showAt(ev) {
    const d = nearest(ev)
    const cx = x(d.t)
    const cy = y(d.n)
    focus.style('display', null)
    focus.select('line').attr('x1', cx).attr('x2', cx)
    focusDot.attr('cx', cx).attr('cy', cy)
    dots.attr('opacity', p => p === d ? 1 : 0.35)
    showTip(htmlFor(d), ev)
  }
  svg.append('rect')
    .attr('class', 'hit')
    .attr('x', m.l)
    .attr('y', m.t)
    .attr('width', Math.max(1, w - m.l - m.r))
    .attr('height', Math.max(1, h - m.t - m.b))
    .attr('fill', 'transparent')
    .style('cursor', 'crosshair')
    .on('pointerenter', showAt)
    .on('pointermove', showAt)
    .on('pointerleave', () => {
      focus.style('display', 'none')
      dots.attr('opacity', 1)
      hideTip()
    })
}

export function heatMap(el, spec, { onClick } = {}) {
  if (!el) return
  clear(el)
  const xs = spec.x || []
  const ys = spec.y || []
  const cells = spec.cells || []
  if (!xs.length || !ys.length || !cells.length) return
  const w = width(el, 720)
  const m = { t: 10, r: 12, b: 52, l: 44 }
  const h = m.t + m.b + Math.max(28, 26 * ys.length)
  const svg = d3.select(el).append('svg').attr('width', w).attr('height', h)
  const x = d3.scaleBand().domain(xs).range([m.l, w - m.r]).padding(0.06)
  const y = d3.scaleBand().domain(ys).range([m.t, h - m.b]).padding(0.1)
  const maxN = d3.max(cells, d => d.n) || 1
  const color = d3.scaleSequential(d3.interpolateYlOrRd).domain([0, maxN])
  const by = new Map(cells.map(d => [`${d.x}\t${d.y}`, d.n]))
  const rects = []
  ys.forEach(yi => {
    xs.forEach(xi => {
      rects.push({ x: xi, y: yi, n: by.get(`${xi}\t${yi}`) || 0 })
    })
  })
  const sel = svg.selectAll('rect.cell').data(rects).enter().append('rect')
    .attr('class', 'cell')
    .attr('x', d => x(d.x)).attr('y', d => y(d.y))
    .attr('width', Math.max(1, x.bandwidth())).attr('height', Math.max(1, y.bandwidth()))
    .attr('rx', 2)
    .attr('fill', d => d.n ? color(d.n) : '#f5f2ec')
  bindTip(sel, d => `<strong>${d.x}</strong><br>depth ${d.y}<br><em>${d.n} pages</em>${d.n ? '<br>click to filter' : ''}`)
  bindClick(sel, d => d.n && onClick && onClick(d))
  const tickEvery = Math.max(1, Math.ceil(xs.length / 8))
  svg.append('g').attr('transform', `translate(0,${h - m.b})`)
    .call(d3.axisBottom(x).tickValues(xs.filter((_, i) => i % tickEvery === 0)))
    .selectAll('text').style('font-size', '10px').attr('transform', 'rotate(-35)').style('text-anchor', 'end')
  svg.append('g').attr('transform', `translate(${m.l},0)`)
    .call(d3.axisLeft(y).tickSize(0))
    .selectAll('text').style('font-size', '10px')
}

export function bubblePack(el, items, { onClick } = {}) {
  if (!el) return
  clear(el)
  const data = rowsOf(items).slice(0, 24)
  if (!data.length) return
  const w = width(el)
  const h = Math.max(220, Math.min(360, 80 + data.length * 14))
  const root = d3.hierarchy({ children: data }).sum(d => d.count).sort((a, b) => b.value - a.value)
  d3.pack().size([w - 8, h - 8]).padding(3)(root)
  const color = d3.scaleOrdinal(SPARK)
  const svg = d3.select(el).append('svg').attr('width', w).attr('height', h)
  const g = svg.append('g').attr('transform', 'translate(4,4)')
  const node = g.selectAll('g').data(root.leaves()).enter().append('g')
    .attr('transform', d => `translate(${d.x},${d.y})`)
  const circles = node.append('circle').attr('r', d => d.r).attr('fill', d => color(d.data.value)).attr('opacity', 0.92)
  bindTip(circles, d => `<strong>${d.data.value}</strong><br>${d.data.count} pages<br><em>click to filter</em>`)
  bindClick(circles, d => onClick && onClick(d.data))
  node.append('text')
    .attr('text-anchor', 'middle').attr('dy', '0.35em')
    .style('font-size', d => `${Math.max(8, Math.min(13, d.r / 3))}px`)
    .style('fill', '#fff8f3')
    .style('pointer-events', 'none')
    .text(d => d.r > 16 ? String(d.data.value).slice(0, 14) : '')
}

export function treeMap(el, items, { onClick } = {}) {
  if (!el) return
  clear(el)
  const data = rowsOf(items).slice(0, 24)
  if (!data.length) return
  const w = width(el, 720)
  const h = 260
  const root = d3.hierarchy({ children: data }).sum(d => d.count).sort((a, b) => b.value - a.value)
  d3.treemap().size([w, h]).paddingInner(3).round(true)(root)
  const color = d3.scaleOrdinal(SPARK)
  const svg = d3.select(el).append('svg').attr('width', w).attr('height', h)
  const node = svg.selectAll('g').data(root.leaves()).enter().append('g')
    .attr('transform', d => `translate(${d.x0},${d.y0})`)
  const rects = node.append('rect')
    .attr('width', d => Math.max(0, d.x1 - d.x0))
    .attr('height', d => Math.max(0, d.y1 - d.y0))
    .attr('fill', d => color(d.data.value))
    .attr('rx', 3)
  bindTip(rects, d => `<strong>${d.data.value}</strong><br>on ${d.data.count} pages<br><em>click to filter</em>`)
  bindClick(rects, d => onClick && onClick(d.data))
  node.append('text')
    .attr('x', 6).attr('y', 16)
    .style('font-size', '11px').style('fill', '#fff8f3').style('pointer-events', 'none')
    .text(d => (d.x1 - d.x0) > 48 && (d.y1 - d.y0) > 18 ? String(d.data.value) : '')
}

export function histogram(el, items) {
  if (!el) return
  clear(el)
  const data = rowsOf(items)
  if (!data.length) return
  const w = width(el)
  const h = 200
  const m = { t: 10, r: 12, b: 40, l: 36 }
  const svg = d3.select(el).append('svg').attr('width', w).attr('height', h)
  const x = d3.scaleBand().domain(data.map(d => d.value)).range([m.l, w - m.r]).padding(0.18)
  const y = d3.scaleLinear().domain([0, d3.max(data, d => d.count) || 1]).nice().range([h - m.b, m.t])
  svg.append('g').attr('transform', `translate(0,${h - m.b})`)
    .call(d3.axisBottom(x)).selectAll('text').style('font-size', '10px')
    .attr('transform', 'rotate(-25)').style('text-anchor', 'end')
  svg.append('g').attr('transform', `translate(${m.l},0)`).call(d3.axisLeft(y).ticks(4))
    .selectAll('text').style('font-size', '10px')
  const bars = svg.selectAll('rect').data(data).enter().append('rect')
    .attr('x', d => x(d.value)).attr('y', d => y(d.count))
    .attr('width', x.bandwidth()).attr('height', d => y(0) - y(d.count))
    .attr('fill', '#e85d04')
  bindTip(bars, d => `<strong>${d.value}</strong><br>${d.count} pages`)
}
