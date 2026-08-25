package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.sp
import com.example.data.FieldPlan
import com.example.data.InventoryItem
import com.example.data.FinanceRecord
import com.example.data.FarmUnit
import com.example.data.FeedPlan
import com.example.data.InventoryMovement
import com.example.ui.theme.ForestGreenPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssetsScreen(
    userRole: String,
    livestock: @Composable () -> Unit,
    inventoryItems: List<InventoryItem>,
    fieldPlans: List<FieldPlan>,
    units: List<FarmUnit>,
    feedPlans: List<FeedPlan>,
    inventoryMovements: List<InventoryMovement>,
    automaticFeedDeductionEnabled: Boolean,
    financeRecords: List<FinanceRecord>,
    onAddInventory: (InventoryItem) -> Unit,
    onAddField: (FieldPlan) -> Unit,
    onHarvest: (FieldPlan, String, Double, Double, String) -> Unit,
    onSaveFeedPlan: (FeedPlan) -> Unit,
    onDeleteFeedPlan: (Long) -> Unit,
    onAutomaticFeedDeductionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }
    var showInventoryDialog by remember { mutableStateOf(false) }
    var showFieldDialog by remember { mutableStateOf(false) }
    var fieldToHarvest by remember { mutableStateOf<FieldPlan?>(null) }
    if (showInventoryDialog) InventoryEntryDialog({ showInventoryDialog=false }, onAddInventory)
    if (showFieldDialog) FieldEntryDialog({ showFieldDialog=false }, onAddField)
    fieldToHarvest?.let { HarvestDialog(it, { fieldToHarvest=null }) { outcome, tonnes, sale, date -> onHarvest(it,outcome,tonnes,sale,date); fieldToHarvest=null } }
    Column(modifier.fillMaxSize()) {
        TabRow(selectedTabIndex=tab) {
            listOf("Livestock", "Inventory", "Fields", "Feed Plans").forEachIndexed { i, label -> Tab(selected=tab==i,onClick={tab=i},text={Text(label,fontWeight=FontWeight.Bold)}) }
        }
        Box(Modifier.weight(1f)) {
            when(tab) {
                0 -> livestock()
                1 -> InventoryContent(inventoryItems)
                2 -> FieldsContent(fieldPlans, onHarvest={fieldToHarvest=it})
                else -> FeedPlansScreen(userRole, automaticFeedDeductionEnabled, units, inventoryItems, feedPlans, inventoryMovements, onAutomaticFeedDeductionChanged, onSaveFeedPlan, onDeleteFeedPlan)
            }
            if (userRole.equals("OWNER",true) && tab in 1..2) FloatingActionButton(
                onClick={ if(tab==1) showInventoryDialog=true else showFieldDialog=true },
                containerColor=ForestGreenPrimary, modifier=Modifier.align(Alignment.BottomEnd).padding(20.dp)
            ) { Icon(Icons.Filled.Add, if(tab==1) "Add inventory item" else "Add field") }
        }
    }
}

@Composable private fun InventoryContent(items: List<InventoryItem>) {
    val low = items.filter { it.minimumThreshold > 0 && it.quantityAvailable <= it.minimumThreshold }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Text("Farm Inventory",fontSize=24.sp,fontWeight=FontWeight.Bold); Text("Stock, inputs, tools and harvested feed",color=Color.Gray)
            if(low.isNotEmpty()) Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF4E5)),modifier=Modifier.fillMaxWidth().padding(top=12.dp)) { Text("Low stock: ${low.joinToString { it.itemName }}",modifier=Modifier.padding(12.dp),color=Color(0xFF92400E)) }
        }
        if(items.isEmpty()) item { EmptyState("No inventory yet", "Use + to record seed, fertiliser, tools, feed, harvest or silage.") }
        items(items,key={it.syncId}) { item ->
            val lowStock=item.minimumThreshold>0 && item.quantityAvailable<=item.minimumThreshold
            Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Filled.Inventory2,null,tint=if(lowStock) Color(0xFFB45309) else ForestGreenPrimary); Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text(item.itemName,fontWeight=FontWeight.Bold); Text("${item.category} • ${item.storageLocation.ifBlank { "Location not set" }}",fontSize=12.sp,color=Color.Gray)
                    if(item.expirationDate.isNotBlank()) Text("Expires: ${item.expirationDate}",fontSize=11.sp,color=Color.Gray) }
                Column(horizontalAlignment=Alignment.End) { Text("${item.quantityAvailable} ${item.unitOfMeasurement}",fontWeight=FontWeight.Bold,color=if(lowStock)Color(0xFFB45309) else Color(0xFF14532D)); if(lowStock)Text("LOW STOCK",fontSize=10.sp,color=Color(0xFFB45309)) }
            }}
        }
    }
}

@Composable private fun FieldsContent(fields: List<FieldPlan>, onHarvest:(FieldPlan)->Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)) {
        item { Text("Planting Fields",fontSize=24.sp,fontWeight=FontWeight.Bold); Text("Plan crops, track harvest windows, then sell or transfer to silage.",color=Color.Gray) }
        if(fields.isEmpty()) item { EmptyState("No fields planned", "Use + to record a maize or crop field, planting date and expected harvest.") }
        items(fields,key={it.syncId}) { field ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Column { Text(field.fieldName,fontWeight=FontWeight.Bold,fontSize=17.sp); Text("${field.cropName}${if(field.variety.isBlank())"" else " • ${field.variety}"}",color=ForestGreenPrimary) }; AssistChip(onClick={},label={Text(field.status)},enabled=false) }
                Text("Planted ${field.plantedDate} • Expected harvest ${field.estimatedHarvestDate}",fontSize=12.sp,color=Color.Gray,modifier=Modifier.padding(top=6.dp))
                Text("${field.sizeAcres} acres • ${field.location.ifBlank { "Location not set" }}",fontSize=12.sp,color=Color.Gray)
                if(field.status=="HARVESTED") Text("Harvested ${field.harvestedTonnes} tonnes → ${field.harvestOutcome}",fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=8.dp))
                else Button(onClick={onHarvest(field)},modifier=Modifier.align(Alignment.End).padding(top=10.dp),colors=ButtonDefaults.buttonColors(containerColor=ForestGreenPrimary)) { Text("Record Harvest") }
            }}
        }
    }
}

@Composable private fun EmptyState(title:String,body:String) { Column(Modifier.fillMaxWidth().padding(34.dp),horizontalAlignment=Alignment.CenterHorizontally) { Text(title,fontWeight=FontWeight.Bold,fontSize=17.sp); Text(body,color=Color.Gray,modifier=Modifier.padding(top=4.dp)) } }

@Composable private fun InventoryEntryDialog(onDismiss:()->Unit,onSave:(InventoryItem)->Unit) {
    var item by remember{mutableStateOf("")}; var category by remember{mutableStateOf("Seeds")}; var sku by remember{mutableStateOf("")}; var desc by remember{mutableStateOf("")}; var qty by remember{mutableStateOf("")}; var unit by remember{mutableStateOf("kg")}; var min by remember{mutableStateOf("0")}; var location by remember{mutableStateOf("")}; var batch by remember{mutableStateOf("")}; var purchase by remember{mutableStateOf(today())}; var expiry by remember{mutableStateOf("")}; var cost by remember{mutableStateOf("")}; var menu by remember{mutableStateOf(false)}
    Dialog(onDismissRequest=onDismiss) { Surface(shape=RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp).fillMaxWidth()) { Text("Inventory Entry",fontSize=20.sp,fontWeight=FontWeight.Bold); Text("Silage is received in tonnes without a cost or finance entry.",fontSize=12.sp,color=Color.Gray)
        Input(item,{item=it},"Item Name *"); Box { OutlinedButton(onClick={menu=true},modifier=Modifier.fillMaxWidth()) { Text("Category: $category") }; DropdownMenu(menu,{menu=false}) { listOf("Seeds","Fertilizers","Pesticides","Tools","Feed","Harvested Crops","Silage","Other").forEach{c->DropdownMenuItem(text={Text(c)},onClick={category=c;menu=false})} } }
        Input(sku,{sku=it},"SKU or Barcode"); Input(desc,{desc=it},"Description"); Row { Input(qty,{qty=it},"Quantity *",Modifier.weight(1f),KeyboardType.Decimal); Spacer(Modifier.width(8.dp)); Input(unit,{unit=it},"Unit",Modifier.weight(1f)) }; Input(min,{min=it},"Minimum Threshold",keyboard=KeyboardType.Decimal); Input(location,{location=it},"Storage Location"); Input(batch,{batch=it},"Batch or Lot Number"); Input(purchase,{purchase=it},"Purchase / Received Date"); Input(expiry,{expiry=it},"Expiration Date (optional)"); if(category!="Silage") Input(cost,{cost=it},"Unit Cost (finance expense)",keyboard=KeyboardType.Decimal)
        Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.End) { TextButton(onClick=onDismiss){Text("Cancel")}; Button(onClick={ if(item.isNotBlank() && (qty.toDoubleOrNull()?:0.0)>0){ onSave(InventoryItem(itemName=item,category=category,skuOrBarcode=sku,description=desc,quantityAvailable=qty.toDoubleOrNull()?:0.0,unitOfMeasurement=if(category=="Silage")"tonnes" else unit,minimumThreshold=min.toDoubleOrNull()?:0.0,storageLocation=location,batchOrLotNumber=batch,purchaseDate=purchase,expirationDate=expiry,unitCost=if(category=="Silage")0.0 else cost.toDoubleOrNull()?:0.0,isSilage=category=="Silage"));onDismiss()}},colors=ButtonDefaults.buttonColors(containerColor=ForestGreenPrimary)){Text("Save Inventory")} }
    }}}
}

@Composable private fun FieldEntryDialog(onDismiss:()->Unit,onSave:(FieldPlan)->Unit) { var name by remember{mutableStateOf("")};var location by remember{mutableStateOf("")};var acres by remember{mutableStateOf("")};var crop by remember{mutableStateOf("Maize")};var variety by remember{mutableStateOf("")};var planted by remember{mutableStateOf(today())};var days by remember{mutableStateOf("120")};var notes by remember{mutableStateOf("")}
Dialog(onDismissRequest=onDismiss){Surface(shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(18.dp).fillMaxWidth()){Text("Add Planting Field",fontSize=20.sp,fontWeight=FontWeight.Bold);Text("Maize commonly takes about 100–150 days. Adjust the duration for your crop and local conditions.",fontSize=12.sp,color=Color.Gray);Input(name,{name=it},"Field Name *");Input(location,{location=it},"Location");Input(acres,{acres=it},"Size (acres)",keyboard=KeyboardType.Decimal);Input(crop,{crop=it},"Crop (e.g. Maize)");Input(variety,{variety=it},"Variety");Input(planted,{planted=it},"Planting Date");Input(days,{days=it},"Days to Harvest",keyboard=KeyboardType.Number);Input(notes,{notes=it},"Planting Notes");Text("Estimated harvest: ${estimate(planted,days.toIntOrNull()?:120)}",fontWeight=FontWeight.SemiBold);Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.End){TextButton(onClick=onDismiss){Text("Cancel")};Button(onClick={if(name.isNotBlank()){onSave(FieldPlan(fieldName=name,location=location,sizeAcres=acres.toDoubleOrNull()?:0.0,cropName=crop, variety=variety,plantedDate=planted,daysToHarvest=days.toIntOrNull()?:120,estimatedHarvestDate=estimate(planted,days.toIntOrNull()?:120),plantingNotes=notes,status="GROWING"));onDismiss()}},colors=ButtonDefaults.buttonColors(containerColor=ForestGreenPrimary)){Text("Save Field")}}}}}
}

@Composable private fun HarvestDialog(field:FieldPlan,onDismiss:()->Unit,onSave:(String,Double,Double,String)->Unit){var outcome by remember{mutableStateOf("SILAGE")};var tonnes by remember{mutableStateOf("")};var sale by remember{mutableStateOf("")};var date by remember{mutableStateOf(today())};Dialog(onDismissRequest=onDismiss){Surface(shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(18.dp).fillMaxWidth()){Text("Harvest ${field.fieldName}",fontSize=20.sp,fontWeight=FontWeight.Bold);Text("Choose Silage to receive tonnes into inventory with no finance record, or Sold to record crop-sale income.",fontSize=12.sp,color=Color.Gray);Row{FilterChip(selected=outcome=="SILAGE",onClick={outcome="SILAGE"},label={Text("Chop as Silage")});Spacer(Modifier.width(8.dp));FilterChip(selected=outcome=="SOLD",onClick={outcome="SOLD"},label={Text("Sold")})};Input(tonnes,{tonnes=it},"Harvested Tonnage *",keyboard=KeyboardType.Decimal);if(outcome=="SOLD")Input(sale,{sale=it},"Total Sale Amount",keyboard=KeyboardType.Decimal);Input(date,{date=it},"Harvest Date");Row(Modifier.fillMaxWidth().padding(top=12.dp),horizontalArrangement=Arrangement.End){TextButton(onClick=onDismiss){Text("Cancel")};Button(onClick={val t=tonnes.toDoubleOrNull()?:0.0;if(t>0.0){onSave(outcome,t,if(outcome=="SOLD")sale.toDoubleOrNull()?:0.0 else 0.0,date)}},colors=ButtonDefaults.buttonColors(containerColor=ForestGreenPrimary)){Text("Confirm Harvest")}}}}}
}
@Composable private fun Input(value:String,onValue:(String)->Unit,label:String,modifier:Modifier=Modifier.fillMaxWidth(),keyboard:KeyboardType=KeyboardType.Text){OutlinedTextField(value=value,onValueChange=onValue,label={Text(label)},singleLine=true,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=keyboard),modifier=modifier.padding(top=6.dp))}
private fun today()=SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(Date())
private fun estimate(date:String,days:Int):String=try{val d=SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).parse(date)?:Date();SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(Calendar.getInstance().apply{time=d;add(Calendar.DAY_OF_YEAR,days)}.time)}catch(_:Exception){""}
