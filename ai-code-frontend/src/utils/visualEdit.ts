/**
 * 可视化编辑工具
 *
 * 方案：主站用绝对 URL + fetch 获取预览 HTML → 注入编辑代码 → iframe.srcdoc
 * 后端 CORS 已全放行，不需要 Vite proxy
 */
import { ref, onUnmounted, type Ref } from 'vue'

export interface SelectedElement {
  tagName: string
  id: string
  className: string
  textContent: string
  cssSelector: string
}

const BACKEND_BASE = 'http://localhost:8445'

const INJECT_STYLE = `
.__viz-hover{outline:2px dashed #1890ff!important;outline-offset:2px}
.__viz-selected{outline:3px solid #e8471c!important;outline-offset:2px}
`

const INJECT_SCRIPT = `
;(function(){
if(window.__vizInjected)return;window.__vizInjected=true
var H='__viz-hover',S='__viz-selected',cur=null
function info(el){
  var t=el.tagName.toLowerCase()
  var id=el.id||''
  var cl=(typeof el.className==='string'?el.className:'')||''
  var tx=(el.textContent||'').trim().substring(0,120)
  var sel=t
  if(id)sel='#'+id
  else if(cl)sel=t+'.'+cl.trim().split(/\\s+/).slice(0,2).join('.')
  return{tagName:t,id:id,className:cl,textContent:tx,cssSelector:sel}
}
document.addEventListener('mouseover',function(e){
  var el=e.target
  if(!el||el.nodeType!==1||el===document.body||el===document.documentElement)return
  if(el===cur)return
  el.classList.add(H)
},true)
document.addEventListener('mouseout',function(e){
  var el=e.target
  if(!el||el.nodeType!==1)return
  if(el===cur)return
  el.classList.remove(H)
},true)
document.addEventListener('click',function(e){
  e.preventDefault();e.stopPropagation()
  var el=e.target
  if(!el||el.nodeType!==1||el===document.body||el===document.documentElement)return
  if(cur)cur.classList.remove(S)
  cur=el;cur.classList.add(S);cur.classList.remove(H)
  window.top.postMessage({type:'VIZ_SELECT',payload:info(cur)},'*')
},true)
window.addEventListener('message',function(e){
  if(!e.data)return
  if(e.data.type==='VIZ_CLEAR'){if(cur){cur.classList.remove(S);cur=null}}
})
})()`

export function useVisualEdit(iframeRef: Ref<HTMLIFrameElement | null>) {
  const isEditMode = ref(false)
  const selectedElements = ref<SelectedElement[]>([])
  const editLoading = ref(false)
  const editError = ref('')
  let savedSrc = ''

  async function enterEditMode(previewUrl: string) {
    const iframe = iframeRef.value
    if (!iframe) {
      editError.value = '预览 iframe 未就绪'
      return
    }

    isEditMode.value = true
    editLoading.value = true
    editError.value = ''
    savedSrc = iframe.src

    // 组装绝对 fetch URL
    let fetchUrl: string
    if (previewUrl.startsWith('/deploy-preview')) {
      // 已部署的 Vue 应用，走 Nginx 代理
      fetchUrl = 'http://localhost' + previewUrl.replace('/deploy-preview', '')
    } else if (previewUrl.startsWith('http')) {
      fetchUrl = previewUrl
    } else {
      fetchUrl = BACKEND_BASE + previewUrl
    }

    try {
      const res = await fetch(fetchUrl)
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      let html = await res.text()

      // 注入 base（保持 CSS/JS/图片相对路径正确）、编辑样式、编辑脚本
      const inject = `<base href="${fetchUrl}"><style>${INJECT_STYLE}</style><script>${INJECT_SCRIPT}</script>`
      if (html.includes('</head>')) {
        html = html.replace('</head>', inject + '</head>')
      } else if (html.includes('<body')) {
        html = html.replace('<body', inject + '<body')
      } else {
        html = inject + html
      }

      iframe.srcdoc = html
      editLoading.value = false
    } catch (e: any) {
      editError.value = `获取预览失败: ${e.message}`
      editLoading.value = false
    }
  }

  function exitEditMode() {
    isEditMode.value = false
    editLoading.value = false
    editError.value = ''
    selectedElements.value = []

    const iframe = iframeRef.value
    if (iframe) {
      iframe.srcdoc = ''
      if (savedSrc) { iframe.src = savedSrc; savedSrc = '' }
    }
  }

  function toggleEditMode(previewUrl: string) {
    isEditMode.value ? exitEditMode() : enterEditMode(previewUrl)
  }

  function onIframeLoad() {}

  function removeElement(index: number) {
    selectedElements.value.splice(index, 1)
    if (selectedElements.value.length === 0) {
      try { iframeRef.value?.contentWindow?.postMessage({ type: 'VIZ_CLEAR' }, '*') } catch { /* ignore */ }
    }
  }

  function clearSelection() {
    selectedElements.value = []
    try { iframeRef.value?.contentWindow?.postMessage({ type: 'VIZ_CLEAR' }, '*') } catch { /* ignore */ }
  }

  function buildElementPrompt(): string {
    if (selectedElements.value.length === 0) return ''
    const parts = selectedElements.value.map((el, i) =>
      [
        `[选中元素 ${i + 1}]`,
        `标签: <${el.tagName}>`,
        `选择器: ${el.cssSelector}`,
        `文本内容: ${el.textContent || '(空)'}`,
        `ID: ${el.id || '(无)'}`,
        `Class: ${el.className || '(无)'}`,
      ].join('\n'),
    )
    return '\n\n--- 以下为用户在页面上选中的元素信息 ---\n' + parts.join('\n\n') + '\n--- 请根据以上元素信息进行针对性修改 ---'
  }

  function handleMessage(e: MessageEvent) {
    if (!e.data || e.data.type !== 'VIZ_SELECT') return
    const el = e.data.payload as SelectedElement
    if (!el || !el.cssSelector) return
    if (selectedElements.value.some((item) => item.cssSelector === el.cssSelector)) return
    selectedElements.value.push(el)
  }

  window.addEventListener('message', handleMessage)
  onUnmounted(() => window.removeEventListener('message', handleMessage))

  return {
    isEditMode,
    selectedElements,
    editLoading,
    editError,
    enterEditMode,
    exitEditMode,
    toggleEditMode,
    removeElement,
    clearSelection,
    buildElementPrompt,
    onIframeLoad,
  }
}
