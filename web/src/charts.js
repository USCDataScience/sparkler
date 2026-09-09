import * as d3 from 'd3'

export function statusBars(el, items) {
  const root = d3.select(el)
  root.selectAll('*').remove()
  const data = (items || []).filter(d => d.count)
  if (!data.length) return
  const w = el.clientWidth || 640
  const h = 220
  const m = { t: 8, r: 12, b: 28, l: 36 }
  const svg = root.append('svg').attr('width', w).attr('height', h)
  const x = d3.scaleBand().domain(data.map(d => d.value)).range([m.l, w - m.r]).padding(0.2)
  const y = d3.scaleLinear().domain([0, d3.max(data, d => d.count) || 1]).nice().range([h - m.b, m.t])
  const color = {
    FETCHED: '#e85d04', UNFETCHED: '#a8a29e', ERROR: '#b91c1c', FILTERED: '#78716c'
  }
  svg.append('g').attr('transform', `translate(0,${h - m.b})`).call(d3.axisBottom(x)).selectAll('text')
    .style('font-size', '11px')
  svg.append('g').attr('transform', `translate(${m.l},0)`).call(d3.axisLeft(y).ticks(4)).selectAll('text')
    .style('font-size', '11px')
  svg.selectAll('rect').data(data).enter().append('rect')
    .attr('x', d => x(d.value)).attr('y', d => y(d.count))
    .attr('width', x.bandwidth()).attr('height', d => y(0) - y(d.count))
    .attr('fill', d => color[d.value] || '#e85d04')
}

export function hostBars(el, items) {
  const root = d3.select(el)
  root.selectAll('*').remove()
  const data = [...(items || [])].sort((a, b) => b.count - a.count).slice(0, 12)
  if (!data.length) return
  const w = el.clientWidth || 640
  const h = Math.max(160, data.length * 22 + 20)
  const m = { t: 8, r: 16, b: 8, l: 140 }
  const svg = root.append('svg').attr('width', w).attr('height', h)
  const y = d3.scaleBand().domain(data.map(d => d.value)).range([m.t, h - m.b]).padding(0.15)
  const x = d3.scaleLinear().domain([0, d3.max(data, d => d.count) || 1]).range([m.l, w - m.r])
  svg.selectAll('rect').data(data).enter().append('rect')
    .attr('x', m.l).attr('y', d => y(d.value))
    .attr('width', d => x(d.count) - m.l).attr('height', y.bandwidth())
    .attr('fill', '#e85d04')
  svg.selectAll('text.host').data(data).enter().append('text')
    .attr('class', 'host')
    .attr('x', m.l - 8).attr('y', d => y(d.value) + y.bandwidth() / 2)
    .attr('text-anchor', 'end').attr('dominant-baseline', 'middle')
    .style('font-size', '11px').text(d => d.value)
  svg.selectAll('text.n').data(data).enter().append('text')
    .attr('class', 'n')
    .attr('x', d => x(d.count) + 4).attr('y', d => y(d.value) + y.bandwidth() / 2)
    .attr('dominant-baseline', 'middle').style('font-size', '11px')
    .text(d => d.count)
}
