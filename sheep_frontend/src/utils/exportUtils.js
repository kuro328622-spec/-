import * as XLSX from "xlsx";

/**
 * 通用 Excel 导出工具
 * @param {Array} data - 需要导出的原始数组数据 (通常是 tableData.value)
 * @param {Object} headerMap - 键值对映射，用于将英文字段名转换为中文表头
 * @param {String} fileName - 导出的文件名 (默认加上 .xlsx)
 */
export const exportToExcel = (data, headerMap, fileName = "报表导出.xlsx") => {
  if (!data || data.length === 0) {
    return false;
  }

  // 1. 处理数据：根据 headerMap 过滤并重命名字段
  const formattedData = data.map((item) => {
    const newItem = {};
    // 遍历映射表，确保导出的顺序和名称符合人类（及AI）的阅读习惯
    Object.keys(headerMap).forEach((key) => {
      // 处理可能存在的 null 或 undefined
      const value = item[key] ?? "无";

      // 特殊处理：如果是数组或对象，转为字符串（例如：分类数组 ['药品', '疫苗'] -> "药品,疫苗"）
      if (Array.isArray(value)) {
        newItem[headerMap[key]] = value.join(", ");
      } else {
        newItem[headerMap[key]] = value;
      }
    });
    return newItem;
  });

  // 2. 创建工作表 (Worksheet)
  const worksheet = XLSX.utils.json_to_sheet(formattedData);

  // 3. 创建工作簿 (Workbook)
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, "数据明细");

  // 4. 输出并下载文件
  XLSX.writeFile(workbook, fileName.endsWith(".xlsx") ? fileName : `${fileName}.xlsx`);

  return true;
};

/**
 * 格式化单条记录为文本（专为 AI 复制设计）
 * @param {Object} row - 行数据
 * @param {Object} headerMap - 映射表
 * @param {String} title - 文本标题
 */
export const formatRecordToText = (row, headerMap, title = "记录详情") => {
  let text = `【${title}】\n`;
  text += `--------------------------\n`;
  Object.keys(headerMap).forEach((key) => {
    const value = row[key] ?? "无";
    text += `${headerMap[key]}：${value}\n`;
  });
  text += `--------------------------\n`;
  text += `生成时间：${new Date().toLocaleString()}\n`;
  return text;
};
