import request from '@/utils/request'

const baseUrl = '/code'

export function runLox(data) {
  return request({
    url: baseUrl + '/lox/run',
    method: 'post',
    data
  })
}
