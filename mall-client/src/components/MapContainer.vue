<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue";
import AMapLoader from "@amap/amap-jsapi-loader";

const props = defineProps({
  traces: {
    type: Array,
    default: () => []
  },
  fromCoord: {
    type: Array,
    default: null
  },
  toCoord: {
    type: Array,
    default: null
  }
})

const originalGetContext = HTMLCanvasElement.prototype.getContext
HTMLCanvasElement.prototype.getContext = function (...args) {
  if (args[0] === '2d') {
    return originalGetContext.call(this, '2d', { willReadFrequently: true })
  }
  return originalGetContext.apply(this, args)
}

let map = null
let polyline = null
let markers = []

const initMap = (AMap) => {
  map = new AMap.Map("container", {
    viewMode: "3D",
    zoom: 5,
    center: [114.30539, 30.59310],
    resizeEnable: true
  })
  map.addControl(new AMap.Scale())
  renderLogisticsRoute(AMap)
}

const clearMarkers = () => {
  if (map) {
    markers.forEach(m => map.remove(m))
    markers = []
  }
  if (polyline) {
    map.remove(polyline)
    polyline = null
  }
}

const renderLogisticsRoute = (AMap) => {
  if (!map || !props.traces || props.traces.length === 0) return

  clearMarkers()

  const path = props.traces.map((t, i) => [t.lng, t.lat])

  polyline = new AMap.Polyline({
    path: path,
    strokeColor: "#1677ff",
    strokeWeight: 4,
    strokeOpacity: 0.7,
    lineJoin: "round",
    showDir: true
  })
  map.add(polyline)
  map.setFitView([polyline], false, [80, 80, 80, 80])

  props.traces.forEach((trace, i) => {
    const isEnd = i === props.traces.length - 1
    const isStart = i === 0
    const marker = new AMap.Marker({
      position: [trace.lng, trace.lat],
      content: `<div style="
        width:${isStart || isEnd ? '28px' : '14px'};
        height:${isStart || isEnd ? '28px' : '14px'};
        background:${isStart ? '#52c41a' : isEnd ? '#1677ff' : '#ff6700'};
        border:3px solid #fff;
        border-radius:50%;
        box-shadow:0 2px 6px rgba(0,0,0,0.3);
      "></div>`,
      offset: new AMap.Pixel(isStart || isEnd ? -14 : -7, isStart || isEnd ? -14 : -7)
    })
    marker.setMap(map)

    let labelContent = trace.location || ""
    if (isStart) labelContent = "起：" + labelContent
    else if (isEnd) labelContent = "终：" + labelContent

    const label = new AMap.Text({
      text: labelContent,
      position: [trace.lng, trace.lat],
      offset: new AMap.Pixel(18, -20),
      style: {
        "font-size": "12px",
        "color": "#333",
        "background": "#fff",
        "padding": "2px 6px",
        "border-radius": "4px",
        "border": "1px solid #e8e8e8",
        "white-space": "nowrap"
      }
    })
    label.setMap(map)
    markers.push(marker, label)
  })
}

watch(() => props.traces, () => {
  if (map && props.traces && props.traces.length > 0) {
    AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY,
      version: "2.0"
    }).then(AMap => {
      renderLogisticsRoute(AMap)
    })
  }
})

onMounted(() => {
  AMapLoader.load({
    key: import.meta.env.VITE_AMAP_KEY,
    version: "2.0",
    plugins: ["AMap.Scale"],
  })
    .then((AMap) => {
      initMap(AMap)
    })
    .catch((e) => {
      console.log(e);
    });
})

onUnmounted(() => {
  map?.destroy();
})
</script>

<template>
  <div id="container"></div>
</template>

<style scoped>
#container {
  width: 100%;
  height: 350px;
}
</style>
