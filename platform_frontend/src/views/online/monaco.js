import tokensProvider from './loxMonarchTokensProvider'
import * as monaco from 'monaco-editor'

// 注册Lox语言
monaco.languages.register({ id: 'lox' })

// 自定义语法高亮
monaco.languages.setMonarchTokensProvider('lox', tokensProvider)

// 自定义代码补全
monaco.languages.setLanguageConfiguration('lox', {
  brackets: [
    ['(', ')'],
    ['{', '}']
  ]
})

export default monaco

