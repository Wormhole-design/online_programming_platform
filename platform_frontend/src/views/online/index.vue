<template>
  <div class="app-wrapper">
    <div class="header">
      <div class="header-left">
        <img :src="logo" class="logo">
        <h1 class="title">{{ title }} </h1>
      </div>
      <div class="header-right">
        <el-button type="primary" plain @click="run">Run（Ctrl/Cmd + Enter）</el-button>
      </div>
    </div>
    <div class="container">
      <div ref="editor" class="editor" />
      <pre ref="output" class="output">{{ result }}</pre>
    </div>
  </div>
</template>

<script>
import { Message } from 'element-ui'
import monaco from './monaco'
import { runLox } from '@/api/online'

export default {
  name: 'Online',
  data() {
    return {
      title: '在线编程平台',
      logo: 'https://wpimg.wallstcn.com/69a1c46c-eb1c-4b46-8bd4-e9e686ef5251.png',
      editor: null,
      result: '',
      isRun: false
    }
  },
  computed: {
  },
  mounted() {
    this.editor = monaco.editor.create(this.$refs.editor, {
      language: 'lox',
      theme: 'vs',
      additionalCssClassName: 'code'
    })
    this.editor.addCommand(
      monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter,
      this.run
    )
  },
  methods: {
    run() {
      if (this.isRun) {
        Message.warning('稍后再试')
        return
      }
      this.isRun = true
      this.result = 'Runing......'
      runLox({ source: this.editor.getValue() }).then(res => {
        // const result = res.data
        // const regex = /^.*Error.*$/gm
        // this.result += result.replace(regex, '<span class="err-report">$&</span>')
        this.result = res.data
        Message.success(res.message)
      }).catch(err => {
        console.log(err)
      }).finally(res => {
        this.isRun = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "~@/styles/mixin.scss";
  @import "~@/styles/variables.scss";

  .app-wrapper {
    @include clearfix;
    position: relative;
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
  }

  .header {
    width: 100%;
    height: 50px;
    overflow: hidden;
    position: fixed;
    z-index: 100;
    top: 0;
    left: 0;
    background: #fff;
    box-shadow: 0 1px 4px rgba(0,21,41,.08);
    display: flex;
    justify-content: space-between;
    align-items: center;

    &-left {
      margin: 15px;
      .logo {
        width: 32px;
        height: 32px;
        vertical-align: middle;
        margin-right: 12px;
      }

      .title {
        display: inline-block;
        margin: 0;
        // color: #fff;
        color: #4FC08D;
        font-weight: 600;
        line-height: 50px;
        font-size: 14px;
        font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
        vertical-align: middle;
      }
    }

    &-right {
      margin: 15px;
    }

  }

  .container {
    padding-top: 50px;
    height: calc(100vh - 100px);
    display: flex;
    flex-direction: row;
    flex: 1;

    .editor, .output {
      flex: 1;
    }
    .editor {
      padding-top: 10px;
      border-right: 1px solid silver;
      white-space: nowrap;
      height: 100%;
    }
    .output {
      padding: 10px;
      font-family: Consolas, monospace;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  }

</style>
