package com.oneliang.ktx.frame.calculator

class CalculateResult(var result: Boolean = false,
                      var calculateResultItemMap: Map<String, CalculateResultItem>,
                      var calculateResultItemMappingMap: Map<String, CalculateResultItem> = calculateResultItemMap,
                      var totalCalculateResultItem: CalculateResultItem = CalculateResultItem())